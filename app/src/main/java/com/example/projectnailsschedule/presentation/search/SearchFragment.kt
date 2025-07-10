package com.example.projectnailsschedule.presentation.search

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.SelectUnifBinding
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.models.DateParams
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.utils.Util
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private val log = this::class.simpleName
    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()

    private var _binding: SelectUnifBinding? = null
    private val binding get() = _binding!!

    private var appointmentList: MutableList<AppointmentModelDb>? = null
    private var searchRvAdapter: SearchRvAdapter? = null

    private var searchView: SearchView? = null
    private var searchRv: RecyclerView? = null
    private var snackbar: Snackbar? = null

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SelectUnifBinding.inflate(inflater, container, false)

        initViews()

        swipeToDelete()

        initClickListeners()

        clearSearchView()

        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")

        return binding.root
    }

    private fun initViews() {
        searchView = binding.searchView
        searchRv = binding.recyclerView
        binding.floatingActionButton.visibility = View.GONE
    }

    private fun initClickListeners() {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // search only after button Search pressed on keyboard
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchQuery = "%$newText%"
                lifecycleScope.launch(Dispatchers.IO) {
                    appointmentList =
                        async { dateParamsViewModel.searchAppointment(searchQuery) }.await()
                    withContext(Dispatchers.Main) {
                        inflateRecyclerVIew(appointmentList!!)
                    }
                }
                return false
            }
        })

        val searchCloseButtonId =
            searchView?.findViewById<View>(androidx.appcompat.R.id.search_close_btn)?.id
        if (searchCloseButtonId != null) {
            searchView?.findViewById<ImageView>(searchCloseButtonId)?.setOnClickListener {
                clearSearchView()
                snackbar?.dismiss()
            }
        }
    }

    private fun inflateRecyclerVIew(appointmentsList: MutableList<AppointmentModelDb>) {
        // create adapter
        searchRvAdapter = SearchRvAdapter(
            appointmentsList = appointmentsList,
            dateParamsViewModel = dateParamsViewModel
        )

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(activity, 1)

        searchRv?.layoutManager = layoutManager
        searchRv?.adapter = searchRvAdapter

        searchRvAdapter!!.setOnItemClickListener(object : SearchRvAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                appointmentClick(position)
            }
        })
    }

    private fun swipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deleteAppointment: AppointmentModelDb = appointmentList!![position]

                // delete client from Db
                lifecycleScope.launch(Dispatchers.IO) {
                    dateParamsViewModel.deleteAppointment(deleteAppointment, position)
                    withContext(Dispatchers.Main) {
                        appointmentList!!.removeAt(position)
                        searchRvAdapter?.notifyItemRemoved(position)
                    }
                }

                // show Snackbar
                snackbar = Snackbar.make(
                    searchRv!!,
                    getString(R.string.deleted_appointment_text, deleteAppointment.name),
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(resources.getColor(R.color.yellow))
                    .setActionTextColor(resources.getColor(R.color.black))
                    .setTextColor(resources.getColor(R.color.black))
                    .setAction(
                        getString(R.string.cancel)
                    ) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            dateParamsViewModel.insertAppointment(deleteAppointment, position)
                            withContext(Dispatchers.Main) {
                                searchRv!!.smoothScrollToPosition(position)
                                appointmentList!!.add(position, deleteAppointment)
                                searchRvAdapter?.notifyItemInserted(position)
                            }
                        }
                    }
                snackbar!!.show()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return super.getSwipeEscapeVelocity(defaultValue) * 10
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val deleteIcon: Drawable? =
                    ContextCompat.getDrawable(requireContext(), R.drawable.baseline_delete_24)!!

                val itemView = viewHolder.itemView
                val iconMarginVertical =
                    (viewHolder.itemView.height - deleteIcon!!.intrinsicHeight) / 2

                val colorDrawableBackground =
                    ColorDrawable(requireContext().resources.getColor(R.color.yellow))

                val left =
                    itemView.right - deleteIcon.intrinsicWidth - deleteIcon.intrinsicWidth // 882
                val right = itemView.right - deleteIcon.intrinsicWidth // 1014
                val top = itemView.top + iconMarginVertical
                val bottom = itemView.bottom - iconMarginVertical

                colorDrawableBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                deleteIcon.setBounds(left, top, right, bottom)

                deleteIcon.level = 0

                colorDrawableBackground.draw(c)
                c.save()

                if (dX > 0)
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                else
                    c.clipRect(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )

                deleteIcon.draw(c)
                c.restore()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }).attachToRecyclerView(searchRv)
    }

    private fun clearSearchView() {
        searchView?.setQuery(null, true)
    }

    override fun onResume() {
        super.onResume()
        ruStoreAd.banner(requireContext(), binding.root, lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbar?.dismiss()
        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")
        _binding = null
    }

    private fun appointmentClick(position: Int) {
        // edit selected appointment
        //dateParamsViewModel.appointmentPosition = position
        lifecycleScope.launch {
            val date = Util().convertStringToLocalDate(appointmentList?.get(position)?.date!!)
            val dateAppointments = dateParamsViewModel.getArrayOfAppointments(date!!)
            val selectedDate = DateParams(
                date = date,
                appointmentsList = dateAppointments
            )
            val currentAppointment = appointmentList!![position]
            dateParamsViewModel.appointmentPosition = dateParamsViewModel.getAppointmentPositionInDate(currentAppointment, dateAppointments)
            dateParamsViewModel.updateSelectedDate(selectedDate)
            findNavController().navigate(R.id.action_nav_search_to_nav_appointment)
        }
    }
}