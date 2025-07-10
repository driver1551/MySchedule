package com.example.projectnailsschedule.presentation.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentCalendarBinding
import com.example.projectnailsschedule.domain.models.DateParams
import com.example.projectnailsschedule.presentation.calendar.calendarRecyclerView.CalendarRvAdapter
import com.example.projectnailsschedule.presentation.calendar.dateShortRecyclerView.DateShortAdapter
import com.example.projectnailsschedule.utils.Util
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment(),
    DateShortAdapter.OnItemListener {
    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private var monthTextView: TextView? = null
    private var yearTextView: TextView? = null
    private var calendarRecyclerView: RecyclerView? = null
    private var shortDataRecyclerView: RecyclerView? = null
    private var addButton: FloatingActionButton? = null

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // set binding
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)

        // init all widgets
        initViews()

        recoverViewState()

        // init click listeners
        initClickListeners()

        // inti observers
        initObservers()

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initViews() {
        // init all widgets
        calendarRecyclerView = binding.calendarRecyclerView
        shortDataRecyclerView = binding.shortDataRecyclerView
        monthTextView = binding.monthTextView
        yearTextView = binding.yearTextView
        addButton = binding.goIntoDate
    }

    private fun initClickListeners() {
        // set click listener on button go_into_date
        binding.goIntoDate.setOnClickListener {
            // start fragment with chosen date )
            binding.goIntoDate.findNavController().navigate(
                R.id.action_nav_calendar_to_dateFragment
            )
        }

        // set click listener on button next_month
        binding.nextMonthButton.setOnClickListener {
            changeMonth(operator = true)
        }

        // set click listener on button previous_month
        binding.prevMonthButton.setOnClickListener {
            changeMonth(operator = false)
        }
    }

    private fun initObservers() {
        // set observer for DateParams
        dateParamsViewModel.selectedDate.observe(viewLifecycleOwner) {
            val previousDate = dateParamsViewModel.previousDate.value

            // Если выбранный год изменился - перерисовать номер и скачать новые данные с сервера
            if (it.date!!.year != previousDate!!.date?.year) {
                // Получить данные за год с сервера
                getYearProductionCalendar()

                // Установить новый номер года
                setYearTextView(it)
            }

            // if month was changed
            if (it.date!!.monthValue != previousDate.date?.monthValue) {
                // update monthTextView
                setMonthTextView(it)
                // update rv
                inflateCalendarRecyclerView(it)
            }

            // if day was changed and has appointments
            if (it.appointmentsList != null) {
                inflateShortDateRecyclerView(it)
            }

            // set previousDate
            dateParamsViewModel.previousDate.value = DateParams(
                date = it.date,
                appointmentsList = it.appointmentsList
            )
        }

        // set date details (fab and shortDateRv) visibility if click was performed
        dateParamsViewModel.dateDetailsVisibility.observe(viewLifecycleOwner) {
            if (it) {
                shortDataRecyclerView?.visibility = View.VISIBLE
                addButton?.visibility = View.VISIBLE
            } else {
                shortDataRecyclerView?.visibility = View.INVISIBLE
                addButton?.visibility = View.INVISIBLE
            }
        }

        // set if selected day is day off observer
        dateParamsViewModel.dateInfo.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.dayOffCl.visibility = View.VISIBLE
                binding.dayOffInfo.text = it

                val icon = dateParamsViewModel.getHolidayIcon(it)
                binding.dayOffIcon.setImageResource(icon)
            } else {
                binding.dayOffCl.visibility = View.GONE
            }
        }
    }

    private fun setMonthTextView(selectedDateParams: DateParams) {
        // update monthTextView
        val date =
            Date.from(selectedDateParams.date?.atStartOfDay(ZoneId.systemDefault())?.toInstant())

        val calendar = Calendar.getInstance()
        calendar.time = date
        val monthResource = when (calendar.get(Calendar.MONTH)) {
            Calendar.JANUARY -> R.string.january
            Calendar.FEBRUARY -> R.string.february
            Calendar.MARCH -> R.string.march
            Calendar.APRIL -> R.string.april
            Calendar.MAY -> R.string.may
            Calendar.JUNE -> R.string.june
            Calendar.JULY -> R.string.july
            Calendar.AUGUST -> R.string.august
            Calendar.SEPTEMBER -> R.string.september
            Calendar.OCTOBER -> R.string.october
            Calendar.NOVEMBER -> R.string.november
            Calendar.DECEMBER -> R.string.december
            else -> 0
        }
        val formattedMonth = getString(monthResource)

        binding.monthTextView.text = formattedMonth
    }

    private fun setYearTextView(selectedDateParams: DateParams) {
        // update yearTextView
        val year = selectedDateParams.date?.year.toString()
        yearTextView?.text = year
    }

    private fun inflateCalendarRecyclerView(selectedDateParams: DateParams) {
        // get array of days from selected month
        val daysInMonth: ArrayList<String> = Util().getArrayFromMonth(selectedDateParams.date!!)
        // create adapter
        val calendarRvAdapter = CalendarRvAdapter(
            daysInMonth = daysInMonth,
            dateParamsViewModel = dateParamsViewModel,
            context = requireContext()
        )

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(activity, 7)

        calendarRecyclerView?.layoutManager = layoutManager
        calendarRecyclerView?.adapter = calendarRvAdapter
        calendarRecyclerView?.scheduleLayoutAnimation()
    }

    private fun inflateShortDateRecyclerView(selectedDateParams: DateParams) {
        // Check if selectedDate has appointments
        if (selectedDateParams.appointmentsList == null ||
            selectedDateParams.appointmentsList!!.isEmpty()
        ) {
            shortDataRecyclerView!!.visibility = View.INVISIBLE
            return
        } else {
            shortDataRecyclerView!!.visibility = View.VISIBLE
        }

        // create adapter for ShortDateRecyclerVIew
        val dateShortAdapter =
            DateShortAdapter(
                selectedDateParams,
                this
            )

        // create layoutManager with 1 elements in a row
        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(activity, 1)

        // set adapter and layout manager to recycler view
        shortDataRecyclerView?.layoutManager = layoutManager
        shortDataRecyclerView?.adapter = dateShortAdapter
    }

    private fun changeMonth(operator: Boolean) {
        dateParamsViewModel.changeMonth(operator = operator)
    }

    override fun onResume() {
        ruStoreAd.banner(requireContext(), binding.root, lifecycleScope)

        super.onResume()

        // hide keyboard
        Util().hideKeyboard(requireActivity())
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // set search button visible
        menu.findItem(R.id.search).isVisible = true
        menu.findItem(R.id.full_month_view).isVisible = true
        super.onPrepareOptionsMenu(menu)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onItemClickShortDate() {
        // start fragment with chosen date
        val bundle = Bundle()
        bundle.putParcelable("dateParams", dateParamsViewModel.selectedDate.value)
        binding.goIntoDate.findNavController().navigate(
            R.id.action_nav_calendar_to_dateFragment,
            bundle
        )
    }

    private fun recoverViewState() {
        dateParamsViewModel.selectedDate.value?.let { inflateCalendarRecyclerView(it) }
        dateParamsViewModel.selectedDate.value?.let { inflateShortDateRecyclerView(it) }
        dateParamsViewModel.selectedDate.value?.let { setMonthTextView(it) }
        dateParamsViewModel.selectedDate.value?.let { setYearTextView(it) }
    }

    private fun getYearProductionCalendar() {
        // Получить данные с сервера за выбранный год
        // TODO: надо добавить флаг, что можно обновляться по дням, пока флаг фолс этого не делать
        CoroutineScope(Dispatchers.IO).launch {
            dateParamsViewModel.getYearProcedureCalendar(dateParamsViewModel.selectedDate.value?.date?.year.toString())
        }
    }
}