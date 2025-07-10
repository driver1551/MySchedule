package com.example.projectnailsschedule.presentation.appointment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentAppointmentBinding
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.models.ClientModelDb
import com.example.projectnailsschedule.domain.models.ProcedureModelDb
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.presentation.clients.ClientsViewModel
import com.example.projectnailsschedule.utils.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AppointmentFragment : Fragment() {
    private val log = this::class.simpleName

    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()
    private val clientsViewModel: ClientsViewModel by activityViewModels()

    private lateinit var currentAppointment: AppointmentModelDb

    private var _binding: FragmentAppointmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var saveToolbarButton: MenuItem

    private var appointmentClientId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            defineAppointment()

            // set current appointmentParams
            inflateViews()
        }

        // set title text
        setTitle()

        setHasOptionsMenu(true)
        dateParamsViewModel.updateUserData("$log ${object {}.javaClass.enclosingMethod?.name}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("client")
            ?.observe(viewLifecycleOwner) {
                clientSelected()
            }

        // select procedure results
        // TODO: переписать через вью модель
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ProcedureModelDb>("procedure")
            ?.observe(viewLifecycleOwner) { savedState ->
                procedureSelected(savedState)
            }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        // add save button
        saveToolbarButton = menu.findItem(R.id.save_toolbar_button)
        saveToolbarButton.isVisible = true

        setClickListeners()
        super.onPrepareOptionsMenu(menu)
    }

    private fun setClickListeners() {
        saveToolbarButton.setOnMenuItemClickListener {
            if (currentAppointment._id == null) {
                // new Appointment
                createAppointment()
            } else {
                // edit Appointment
                editAppointment()
            }
            true
        }

        with(binding) {
            // set ClickListener on day_edit_text
            dayEditText.setOnClickListener {
                if (dateParamsViewModel.spinnerSelected()) {
                    setSpinnerDatePicker()
                } else {
                    setDatePicker()
                }
            }

            // set ClickListener on time_edit_text
            timeEditText.setOnClickListener {
                if (dateParamsViewModel.spinnerSelected()) {
                    setSpinnerTimePicker()
                } else {
                    setTimePicker()
                }
            }

            selectClientButton.setOnClickListener {
                val dialogFragment = SelectClientFragment()
                dialogFragment.show(parentFragmentManager, "SelectClientFragment")
            }

            procedureSelectButton.setOnClickListener {
                val dialogFragment = SelectProcedureFragment()
                dialogFragment.show(parentFragmentManager, "SelectProcedureFragment")
            }

            clearClientButton.setOnClickListener {
                unBlockClientFields()
                clearClientFields()

                clientsViewModel.selectedClient = null

                Toast.makeText(requireContext(), "Данные о клиенте очищены", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun setTitle() {
        val title: TextView = binding.fragmentAppointmentTitle
        val date: TextView = binding.fragmentAppointmentDate
        date.text = Util().formatDateToRus(dateParamsViewModel.selectedDate.value?.date!!)

        if (currentAppointment._id == null) {
            // no _id - add new Appointment
            title.text = getString(R.string.new_appointment_text)
        } else {
            // _id - edit Appointment
            title.text = getString(R.string.change_appointment_text)
        }
    }

    private fun createAppointment() {
        // create appointmentParams object
        with(binding) {
            currentAppointment = AppointmentModelDb(
                _id = null,
                date = dayEditText.text.toString(),
                clientId = appointmentClientId,
                name = nameEt.text.toString(),
                time = timeEditText.text.toString(),
                clientNotes = clientNotesEt.text.toString(),
                procedure = procedureEt.text.toString(),
                procedurePrice = procedurePriceEt.text.toString(),
                procedureNotes = null,
                phone = clientPhoneEt.text.toString(),
                vk = clientVkLinkEt.text.toString(),
                telegram = clientTelegramEt.text.toString(),
                instagram = clientInstagramEt.text.toString(),
                whatsapp = clientWhatsappEt.text.toString(),
                notes = appointmentNotesEt.text.toString(),
                photo = clientsViewModel.selectedClient?.photo,
                deleted = false
            )

            lifecycleScope.launch {
                currentAppointment._id = dateParamsViewModel.insertAppointment(currentAppointment)
            }

            val toast: Toast = Toast.makeText(
                context,
                "${currentAppointment.date}\n${getString(R.string.toast_created)}",
                Toast.LENGTH_LONG
            )
            toast.show()
        }

        // Return to previous screen
        findNavController().popBackStack()
    }

    private fun editAppointment() {
        // create appointmentParams object
        with(binding) {
            val updateAppointment = currentAppointment.copy(
                date = dayEditText.text.toString(),
                clientId = appointmentClientId,
                name = nameEt.text.toString(),
                time = timeEditText.text.toString(),
                clientNotes = clientNotesEt.text.toString(),
                procedure = procedureEt.text.toString(),
                procedurePrice = procedurePriceEt.text.toString(),
                phone = clientPhoneEt.text.toString(),
                vk = clientVkLinkEt.text.toString(),
                telegram = clientTelegramEt.text.toString(),
                instagram = clientInstagramEt.text.toString(),
                whatsapp = clientWhatsappEt.text.toString(),
                notes = appointmentNotesEt.text.toString(),
                photo = clientsViewModel.selectedClient?.photo,
            )

            lifecycleScope.launch {
                dateParamsViewModel.updateAppointment(updateAppointment)
            }

            Toast.makeText(context, getString(R.string.toast_edited), Toast.LENGTH_LONG).show()

            findNavController().popBackStack()
        }
    }

    private fun inflateViews() {
        // if appointment position != null - inflate view
        if (dateParamsViewModel.appointmentPosition != null) {
            with(currentAppointment) {
                binding.dayEditText.text = this.date
                binding.timeEditText.text = this.time
                binding.appointmentNotesEt.setText(this.notes)

                binding.procedureEt.setText(this.procedure)
                binding.procedurePriceEt.setText(this.procedurePrice)
                //binding.procedureNotesEt.setText(this.procedureNotes)
                binding.nameEt.setText(this.name)

                // set client views (old)
                binding.clientPhoneEt.setText(this.phone)
                binding.clientVkLinkEt.setText(this.vk)
                binding.clientTelegramEt.setText(this.telegram)
                binding.clientInstagramEt.setText(this.instagram)
                binding.clientWhatsappEt.setText(this.whatsapp)
                binding.clientAvatarDateAppointment.setImageURI(this.photo?.toUri())
                binding.clientNotesEt.setText(this.clientNotes)
            }
        } else {
            binding.dayEditText.text =
                Util().formatDateToRus(dateParamsViewModel.selectedDate.value?.date!!)
        }

        // if client has been set before - fill views with client`s data
        if (appointmentClientId != null) {
            // set client views (new)
            binding.clientPhoneEt.setText(clientsViewModel.selectedClient!!.phone)
            binding.clientVkLinkEt.setText(clientsViewModel.selectedClient!!.vk)
            binding.clientTelegramEt.setText(clientsViewModel.selectedClient!!.telegram)
            binding.clientInstagramEt.setText(clientsViewModel.selectedClient!!.instagram)
            binding.clientWhatsappEt.setText(clientsViewModel.selectedClient!!.whatsapp)
            binding.clientAvatarDateAppointment.setImageURI(clientsViewModel.selectedClient!!.photo?.toUri())
            binding.clientNotesEt.setText(clientsViewModel.selectedClient!!.notes)
        }
    }

    private fun setDatePicker() {
        // set datePicker to select date field
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, pickedYear, pickedMonth, pickedDay ->
                val date = Util().dateConverter("$pickedDay.${pickedMonth + 1}.$pickedYear")
                binding.dayEditText.text = date
            }, year, month, day
        )
        datePickerDialog.show()
    }

    private fun setTimePicker() {
        // clocks time picker
        val calendar = Calendar.getInstance()
        val mTimePicker: TimePickerDialog
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        mTimePicker = TimePickerDialog(
            context, { _, pickedHour, pickedMinute ->
                val time = String.format("%02d:%02d", pickedHour, pickedMinute)
                binding.timeEditText.text = time
            }, hour, minute, true
        )
        mTimePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dateParamsViewModel.updateUserData("$log ${object {}.javaClass.enclosingMethod?.name}")
    }

    private suspend fun defineAppointment() {
        // set current appointmentParams
        if (dateParamsViewModel.appointmentPosition != null) {
            // if AppointmentModelDb already exists
            currentAppointment =
                dateParamsViewModel.selectedDate.value!!.appointmentsList!![dateParamsViewModel.appointmentPosition!!]

            if (currentAppointment.clientId == null) {
                // client is not in the Client database
                clientsViewModel.selectedClient = ClientModelDb(
                    name = currentAppointment.name,
                    phone = currentAppointment.phone,
                    telegram = currentAppointment.telegram,
                    instagram = currentAppointment.instagram,
                    vk = currentAppointment.vk,
                    whatsapp = currentAppointment.whatsapp,
                    photo = currentAppointment.photo
                )
            } else {
                // Get client data from the Client database
                // TODO: предусмотреть, если килент из базы был удален

                clientsViewModel.selectedClient =
                    clientsViewModel.getClientById(currentAppointment.clientId!!)
                appointmentClientId = clientsViewModel.selectedClient!!._id

                blockClientFields()
            }
        } else {
            // new AppointmentModelDb
            currentAppointment = AppointmentModelDb(deleted = false)
            clientsViewModel.selectedClient = null
        }
    }

    private fun clientSelected() {
        // update views
        with(binding) {
            nameEt.setText(clientsViewModel.selectedClient!!.name)
            clientPhoneEt.setText(clientsViewModel.selectedClient!!.phone)
            clientVkLinkEt.setText(clientsViewModel.selectedClient!!.vk)
            clientTelegramEt.setText(clientsViewModel.selectedClient!!.telegram)
            clientInstagramEt.setText(clientsViewModel.selectedClient!!.instagram)
            clientWhatsappEt.setText(clientsViewModel.selectedClient!!.whatsapp)
            clientNotesEt.setText(clientsViewModel.selectedClient!!.notes)
            clientAvatarDateAppointment.setImageURI(clientsViewModel.selectedClient!!.photo?.toUri())

            Util().animateEditTexts(
                nameEt,
                clientPhoneEt,
                clientVkLinkEt,
                clientTelegramEt,
                clientInstagramEt,
                clientWhatsappEt,
                clientNotesEt
            )
        }

        // set selected client id
        appointmentClientId = clientsViewModel.selectedClient!!._id

        blockClientFields()
    }

    private fun procedureSelected(savedState: ProcedureModelDb) {
        // update views
        with(binding) {
            procedureEt.setText(savedState.procedureName)
            procedurePriceEt.setText(savedState.procedurePrice)
            //procedureNotesEt.setText(savedState.procedureNotes)
            Util().animateEditTexts(procedureEt)
            Util().animateEditTexts(procedurePriceEt)
            //Util().animateEditTexts(procedureNotesEt)
        }
    }

    private fun blockClientFields() {
        with(binding) {
            setGrayBackground(nameEt)
            disableTouchMode(nameEt)

            setGrayBackground(clientPhoneEt)
            disableTouchMode(clientPhoneEt)

            setGrayBackground(clientVkLinkEt)
            disableTouchMode(clientVkLinkEt)

            setGrayBackground(clientTelegramEt)
            disableTouchMode(clientTelegramEt)

            setGrayBackground(clientInstagramEt)
            disableTouchMode(clientInstagramEt)

            setGrayBackground(clientWhatsappEt)
            disableTouchMode(clientWhatsappEt)

            setGrayBackground(clientNotesEt)
            disableTouchMode(clientNotesEt)
        }
    }

    private fun unBlockClientFields() {
        with(binding) {
            setWhiteBackground(nameEt)
            enableTouchMode(nameEt)

            setWhiteBackground(clientPhoneEt)
            enableTouchMode(clientPhoneEt)

            setWhiteBackground(clientVkLinkEt)
            enableTouchMode(clientVkLinkEt)

            setWhiteBackground(clientTelegramEt)
            enableTouchMode(clientTelegramEt)

            setWhiteBackground(clientInstagramEt)
            enableTouchMode(clientInstagramEt)

            setWhiteBackground(clientWhatsappEt)
            enableTouchMode(clientWhatsappEt)

            setWhiteBackground(clientNotesEt)
            enableTouchMode(clientNotesEt)
        }
    }

    private fun clearClientFields() {
        with(binding) {
            nameEt.setText("")
            clientPhoneEt.setText("")
            clientVkLinkEt.setText("")
            clientTelegramEt.setText("")
            clientInstagramEt.setText("")
            clientWhatsappEt.setText("")
            clientAvatarDateAppointment.setImageResource(R.drawable.client_avatar)
            clientNotesEt.setText("")
        }
        appointmentClientId = null
    }

    private fun setGrayBackground(editText: EditText) {
        val grayBackground = R.drawable.rectangle_2_gray
        editText.setBackgroundResource(grayBackground)
    }

    private fun setWhiteBackground(editText: EditText) {
        val grayBackground = R.drawable.white_rectangle_shape
        editText.setBackgroundResource(grayBackground)
    }

    private fun disableTouchMode(editText: EditText) {
        editText.isFocusableInTouchMode = false
    }

    private fun enableTouchMode(editText: EditText) {
        editText.isFocusableInTouchMode = true
    }

    private fun setSpinnerTimePicker() {
        // spinner time picker
        val myCalender = Calendar.getInstance()
        val hour = myCalender[Calendar.HOUR_OF_DAY]
        val minute = myCalender[Calendar.MINUTE]
        val myTimeListener =
            OnTimeSetListener { view, hourOfDay, minute ->
                if (view.isShown) {
                    myCalender[Calendar.HOUR_OF_DAY] = hourOfDay
                    myCalender[Calendar.MINUTE] = minute
                    val time = String.format("%02d:%02d", hourOfDay, minute)
                    binding.timeEditText.text = time
                }
            }
        val timePickerDialog = TimePickerDialog(
            activity,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            myTimeListener,
            hour,
            minute,
            true
        )

        timePickerDialog.setTitle("Выберите время:")
        timePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        timePickerDialog.show()
    }

    private fun setSpinnerDatePicker() {
        // spinner date picker
        val myCalendar = Calendar.getInstance()
        val year = myCalendar[Calendar.YEAR]
        val month = myCalendar[Calendar.MONTH]
        val day = myCalendar[Calendar.DAY_OF_MONTH]

        val myDateListener = DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDay ->
            if (view.isShown) {
                myCalendar.set(Calendar.YEAR, selectedYear)
                myCalendar.set(Calendar.MONTH, selectedMonth)
                myCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                val date = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.dayEditText.text = date
            }
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            myDateListener,
            year,
            month,
            day
        )

        datePickerDialog.setTitle("Выберите дату:")
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.show()
    }

}