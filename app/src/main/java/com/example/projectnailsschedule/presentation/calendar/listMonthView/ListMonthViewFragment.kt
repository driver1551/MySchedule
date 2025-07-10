package com.example.projectnailsschedule.presentation.calendar.listMonthView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentFullMonthViewBinding
import com.example.projectnailsschedule.domain.models.DateParams
import com.example.projectnailsschedule.domain.models.DateWeekAppModel
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.presentation.calendar.listMonthView.fullMonthViewRV.FullMonthViewRVAdapter
import com.example.projectnailsschedule.utils.Util
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.exoplayer2.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class ListMonthViewFragment : Fragment() {
    private val log = this::class.simpleName
    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()

    private var _binding: FragmentFullMonthViewBinding? = null
    private var fullMonthAppointmentsRV: RecyclerView? = null
    private var fullMonthAppointmentsRVAdapter: FullMonthViewRVAdapter? = null

    private var layoutManager: LayoutManager? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullMonthViewBinding.inflate(inflater, container, false)

        initViews()

        initObservers()

        initClickListeners()

        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")

        return binding.root
    }

    private fun initViews() {
        fullMonthAppointmentsRV = binding.fullMonthAppointmentsRV

    }

    private fun initObservers() {
        dateParamsViewModel.selectedDate.observe(viewLifecycleOwner) {
            val previousDate = dateParamsViewModel.previousDate.value

            // if year was changed
            if (it.date!!.year != previousDate!!.date?.year) {
                // update yearTextView
                setYearTextView(it)
            }

            // if month was changed
            if (it.date!!.monthValue != previousDate.date?.monthValue) {
                // update monthTextView
                setMonthTextView(it)
                // update rv
                inflateCalendarRecyclerView(it)
            }

            // set previousDate
            dateParamsViewModel.previousDate.value = DateParams(
                date = it.date,
                appointmentsList = it.appointmentsList
            )
        }
    }

    private fun inflateCalendarRecyclerView(selectedDate: DateParams) {
        // get array of days from selected month
        val daysInMonth: ArrayList<String> = Util().getArrayFromMonth2(selectedDate.date!!)

        val list = mutableListOf<DateWeekAppModel>()

        lifecycleScope.launch {
            for (i in 1..selectedDate.date!!.lengthOfMonth()) {
                // формируем первый день месяца и созадем с ним DateParams
                val dateFormat =
                    Util().dateConverterNew(dateParamsViewModel.getSelectedMonth().toString())
                var date = dateFormat.drop(2)

                date = daysInMonth[i - 1] + date
                date = Util().dateConverter(date)

                Log.d("ListMonthMutableList", date)

                val localDate = Util().convertStringToLocalDate(date)
                val dateParams = DateParams(date = localDate)

                // добавляем в лист объект DateWeekAppModel, который содержит список записей по дню
                val addToList = DateWeekAppModel(
                    date = dateParams.date!!,
                    weekDay = Util().getDayOfWeek(date, requireContext()),
                    appointmentsList = dateParamsViewModel.getArrayOfAppointments(dateParams.date!!)
                )
                list.add(addToList)
            }
            withContext(Dispatchers.Main) {
                // create adapter
                fullMonthAppointmentsRVAdapter =
                    FullMonthViewRVAdapter(
                        list,
                        requireContext(),
                        findNavController(),
                        dateParamsViewModel
                    )

                layoutManager = GridLayoutManager(activity, 1)

                fullMonthAppointmentsRV?.layoutManager = layoutManager
                fullMonthAppointmentsRV?.adapter = fullMonthAppointmentsRVAdapter
                fullMonthAppointmentsRV?.scheduleLayoutAnimation()
            }
        }
    }

    private fun initClickListeners() {
        binding.nextMonthButton.setOnClickListener {
            changeMonth(operator = true)
        }

        binding.prevMonthButton.setOnClickListener {
            changeMonth(operator = false)
        }
    }

    override fun onResume() {
        super.onResume()
        // scroll to previous position
        if (layoutManager != null) {
            fullMonthAppointmentsRV?.post {
                val oldPosition = dateParamsViewModel.oldPosition
                val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                    override fun getVerticalSnapPreference(): Int {
                        return SNAP_TO_START
                    }
                }
                smoothScroller.targetPosition = oldPosition
                layoutManager!!.startSmoothScroll(smoothScroller)
            }
        }
        ruStoreAd.banner(requireContext(), binding.root, lifecycleScope)

        recoverViewState()
    }

    private fun setYearTextView(selectedDateParams: DateParams) {
        val year = selectedDateParams.date?.year.toString()
        binding.yearTextView.text = year
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

    private fun changeMonth(operator: Boolean) {
        dateParamsViewModel.changeMonth(operator = operator)
    }

    private fun recoverViewState() {
        dateParamsViewModel.selectedDate.value?.let { inflateCalendarRecyclerView(it) }
        dateParamsViewModel.selectedDate.value?.let { setMonthTextView(it) }
        dateParamsViewModel.selectedDate.value?.let { setYearTextView(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        fullMonthAppointmentsRVAdapter?.snackbar?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")
        _binding = null
    }
}