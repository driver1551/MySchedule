package com.example.projectnailsschedule.presentation.date

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentDateBinding
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.presentation.date.dateRecyclerView.DateAdapter
import com.example.projectnailsschedule.utils.Util
import com.example.projectnailsschedule.utils.rustore.RuStoreAd
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DateFragment : Fragment() {
    private val log = this::class.simpleName
    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()

    private var _binding: FragmentDateBinding? = null
    private val binding get() = _binding!!

    private var appointmentsRvAdapter: DateAdapter? = null
    private var appointmentsRv: RecyclerView? = null

    private var snackbar: Snackbar? = null

    @Inject
    lateinit var ruStoreAd: RuStoreAd

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDateBinding.inflate(inflater, container, false)

        initViews()

        inflateViews()

        initClickListeners()

        swipeToDelete()

        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")

        return binding.root
    }

    private fun initViews() {
        appointmentsRv = binding.scheduleRecyclerView
    }

    private fun inflateViews() {
        if (dateParamsViewModel.selectedDate.value?.appointmentsList?.size == null) {
            binding.fragmentDateTitle.text = requireContext().getString(R.string.no_data_title)
        } else {
            binding.fragmentDateTitle.text =
                requireContext().getString(R.string.fragment_date_title)
            inflateAppointmentsRV()
        }

        binding.fragmentDateDate.text =
            Util().formatDateToRus(dateParamsViewModel.selectedDate.value?.date!!)
    }

    private fun initClickListeners() {
        // add new appointment
        binding.fragmentDateAddButton.setOnClickListener {
            dateParamsViewModel.appointmentPosition = null
            it.findNavController().navigate(R.id.action_dateFragment_to_appointmentFragment)
        }
    }

    private fun inflateAppointmentsRV() {
        // create adapter
        val appointmentList = dateParamsViewModel.selectedDate.value?.appointmentsList
        appointmentsRvAdapter = DateAdapter(
            appointmentsList = appointmentList!!,
            dateParamsViewModel = dateParamsViewModel
        )

        val layoutManager: RecyclerView.LayoutManager =
            GridLayoutManager(activity, 1)

        appointmentsRv?.layoutManager = layoutManager
        appointmentsRv?.adapter = appointmentsRvAdapter
        appointmentsRv?.scheduleLayoutAnimation()

        // set clickListener on dateRV
        appointmentsRvAdapter!!.setOnItemClickListener(object : DateAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // edit selected appointment
                dateParamsViewModel.appointmentPosition = position
                findNavController().navigate(R.id.action_dateFragment_to_appointmentFragment)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        Util().hideKeyboard(requireActivity())

        ruStoreAd.banner(requireContext(), binding.root, lifecycleScope)
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
                val appointmentList = dateParamsViewModel.selectedDate.value?.appointmentsList!!
                val deleteAppointmentModelDb: AppointmentModelDb =
                    appointmentList[position]

                // delete client from Db
                CoroutineScope(Dispatchers.IO).launch {
                    dateParamsViewModel.deleteAppointment(deleteAppointmentModelDb, position)
                    withContext(Dispatchers.Main) {
                        appointmentsRvAdapter?.notifyItemRemoved(position)
                    }
                }

                // show Snackbar
                snackbar = Snackbar.make(
                    appointmentsRv!!,
                    getString(R.string.deleted_appointment_text, deleteAppointmentModelDb.name),
                    Snackbar.LENGTH_LONG
                ).setBackgroundTint(resources.getColor(R.color.yellow))
                    .setActionTextColor(resources.getColor(R.color.black))
                    .setTextColor(resources.getColor(R.color.black))
                    .setAction(
                        getString(R.string.cancel)
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            dateParamsViewModel.insertAppointment(
                                deleteAppointmentModelDb,
                                position
                            )
                            withContext(Dispatchers.Main) {
                                appointmentsRvAdapter?.notifyItemInserted(position)
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

                val colorDrawableBackground = ColorDrawable(resources.getColor(R.color.yellow))

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
        }).attachToRecyclerView(appointmentsRv)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snackbar?.dismiss()
        dateParamsViewModel.updateUserData("$log ${object{}.javaClass.enclosingMethod?.name}")
        _binding = null
    }
}

