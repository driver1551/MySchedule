package com.example.projectnailsschedule.presentation.clients.editClient

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.databinding.FragmentClientEditBinding
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.presentation.clients.ClientsViewModel
import com.example.projectnailsschedule.utils.Util
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class ClientEditFragment : Fragment() {
    private val clientsViewModel: ClientsViewModel by activityViewModels()
    private val dateParamsViewModel: DateParamsViewModel by activityViewModels()

    private var _binding: FragmentClientEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var nameEt: EditText
    private lateinit var phoneEt: EditText
    private lateinit var vkEt: EditText
    private lateinit var telegramEt: EditText
    private lateinit var instagramEt: EditText
    private lateinit var whatsappEt: EditText
    private lateinit var notesEt: EditText
    private lateinit var clientPhoto: ImageView

    private var tempPhotoFile: File? = null

    private lateinit var saveToolbarButton: MenuItem

    private val REQUEST_READ_CONTACTS = 1
    private val REQUEST_CONTACT_PICK = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientEditBinding.inflate(inflater, container, false)

        initViews()

        inflateViews()

        shortUrl()

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun initViews() {
        nameEt = binding.clientNameEt
        phoneEt = binding.clientPhoneTv
        vkEt = binding.clientVkTv
        telegramEt = binding.clientTgTv
        instagramEt = binding.clientInstagramTv
        whatsappEt = binding.clientWhatsappTv
        notesEt = binding.clientNoteTv
        clientPhoto = binding.clientPhoto
    }

    private fun inflateViews() {
        if (clientsViewModel.selectedClient != null) {
            // inflate views with select client fields
            with(clientsViewModel.selectedClient!!) {
                nameEt.setText(name)
                phoneEt.setText(phone)
                vkEt.setText(vk)
                telegramEt.setText(telegram)
                instagramEt.setText(instagram)
                whatsappEt.setText(whatsapp)
                notesEt.setText(notes)

                // set actual photo
                if (!photo.isNullOrEmpty()) {
                    clientPhoto.setImageURI(photo.toUri())
                }
            }
        }
    }

    private fun setClickListeners() {
        saveToolbarButton.setOnMenuItemClickListener {

            setDataIntoSelectedClient()

            lifecycleScope.launch {
                if (clientsViewModel.selectedClient?._id == null) {
                    // if current client id is null -> create new client in db
                    val id = clientsViewModel.insertClient(clientsViewModel.selectedClient!!)
                    setNewClientId(id)
                } else {
                    // if current client id is not null -> update client in db
                    clientsViewModel.updateClient(clientsViewModel.selectedClient!!)
                    clientsViewModel.updateClientInAppointments(clientsViewModel.selectedClient!!)
                }

                // if new photo has been set
                if (tempPhotoFile != null) {
                    copyPhotoIntoClientFolder()
                    clientsViewModel.updateClient(clientsViewModel.selectedClient!!)
                    clientsViewModel.updateClientInAppointments(clientsViewModel.selectedClient!!)
                }

                updateAppointmentListInSelectedDate()

                showToastSaved()

                findNavController().popBackStack()
            }
            return@setOnMenuItemClickListener true
        }

        binding.allAppointmentsCl.setOnClickListener {
            Toast.makeText(requireContext(), "Функционал в разработке", Toast.LENGTH_LONG).show()
        }

        binding.statisticCl.setOnClickListener {
            Toast.makeText(requireContext(), "Функционал в разработке", Toast.LENGTH_LONG).show()
        }

        binding.cameraIcon.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare() //Crop image(Optional), Check Customization for more option
                .compress(1024) //Final image size will be less than 1 MB(Optional)
                .maxResultSize(512, 512)
                .saveDir(File(requireContext().filesDir, "ClientAvatarTemp"))
                .start()
        }

        // OnCLickListener - showOptionsDialog

        phoneEt.setOnClickListener {
            showOptionsDialog(phoneEt)
        }

        vkEt.setOnClickListener {
            showOptionsDialog(vkEt)
        }

        instagramEt.setOnClickListener {
            showOptionsDialog(instagramEt)
        }

        telegramEt.setOnClickListener {
            showOptionsDialog(telegramEt)
        }

        whatsappEt.setOnClickListener {
            showOptionsDialog(whatsappEt)
        }

        // OnEditorActionListener

        phoneEt.setOnEditorActionListener { _, i, _ ->
            onEditorActionListener(i, phoneEt)
        }

        vkEt.setOnEditorActionListener { _, i, _ ->
            val shortUrl = Util().extractVkUsername(vkEt.text.toString())
            vkEt.setText(shortUrl)
            onEditorActionListener(i, vkEt)
        }

        instagramEt.setOnEditorActionListener { _, i, _ ->
            val shortUrl = Util().extractInstagramUsername(instagramEt.text.toString())
            instagramEt.setText(shortUrl)
            onEditorActionListener(i, instagramEt)
        }

        telegramEt.setOnEditorActionListener { _, i, _ ->
            onEditorActionListener(i, telegramEt)
        }

        whatsappEt.setOnEditorActionListener { _, i, _ ->
            onEditorActionListener(i, whatsappEt)
        }

        // OnFocusChangeListener

        phoneEt.setOnFocusChangeListener { _, hasFocus ->
            onFocusChangeListener(hasFocus, phoneEt)
        }

        vkEt.setOnFocusChangeListener { _, hasFocus ->
            val shortUrl = Util().extractVkUsername(vkEt.text.toString())
            vkEt.setText(shortUrl)

            onFocusChangeListener(hasFocus, vkEt)
        }

        instagramEt.setOnFocusChangeListener { _, hasFocus ->
            val shortUrl = Util().extractInstagramUsername(instagramEt.text.toString())
            instagramEt.setText(shortUrl)
            onFocusChangeListener(hasFocus, instagramEt)
        }

        telegramEt.setOnFocusChangeListener { _, hasFocus ->
            onFocusChangeListener(hasFocus, telegramEt)
        }

        whatsappEt.setOnFocusChangeListener { _, hasFocus ->
            onFocusChangeListener(hasFocus, whatsappEt)
        }

        binding.clientPhoneTv.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.clientTgTv.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.clientWhatsappTv.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clientsViewModel.selectedClient = null
        _binding = null
        deleteTempFolder()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        saveToolbarButton = menu.findItem(R.id.save_toolbar_button)
        saveToolbarButton.isVisible = true
        setClickListeners()
        super.onPrepareOptionsMenu(menu)
    }

    private fun shortUrl() {
        if (vkEt.text.toString().contains("https://vk.com/")) {
            val shortUrl = Util().extractVkUsername(vkEt.text.toString())
            vkEt.setText(shortUrl)
        }

        if (instagramEt.text.toString().contains("https://www.instagram.com/")) {
            val shortUrl =
                Util().extractInstagramUsername(clientsViewModel.selectedClient?.instagram.toString())
            instagramEt.setText(shortUrl)
        }

        if (telegramEt.text.toString().contains("https://t.me/")) {
            val shortUrl = Util().extractTelegramUsername(telegramEt.text.toString())
            telegramEt.setText(shortUrl)
        }
    }

    private fun showOptionsDialog(clickedView: EditText) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_dialog, null)
        bottomSheetDialog.setContentView(view)

        val execute = view.findViewById<TextView>(R.id.execute)
        val edit = view.findViewById<TextView>(R.id.edit)

        when (clickedView.id) {
            // обработка в зависимости от клика пользователя
            phoneEt.id -> {
                execute.text = "Вызов"
                view.findViewById<ImageView>(R.id.icon)
                    .setImageResource(R.drawable.baseline_call_24)

                // Выбор из телефонной книги
                view.findViewById<ImageView>(R.id.get_phone_from_contacts_icon).visibility =
                    View.VISIBLE
                view.findViewById<TextView>(R.id.get_phone_from_contacts_textView).visibility =
                    View.VISIBLE

                view.findViewById<TextView>(R.id.get_phone_from_contacts_textView)
                    .setOnClickListener {
                        try {
                            checkContactPermission()
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Возникла непредвиденная ошибка",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        bottomSheetDialog.dismiss()
                    }
            }

            vkEt.id -> {
                view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.vk_logo)
            }

            telegramEt.id -> {
                view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.telegram_logo)

            }

            instagramEt.id -> {
                view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.instagram_logo)
            }

            whatsappEt.id -> {
                view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.whatsapp_logo)
            }
        }

        execute.setOnClickListener {
            // Execute
            when (clickedView.id) {
                phoneEt.id -> {
                    clientsViewModel.startPhone(phoneEt.text.toString())
                    view.findViewById<ImageView>(R.id.icon)
                        .setImageResource(R.drawable.baseline_call_24)
                }

                vkEt.id -> {
                    clientsViewModel.startVk(vkEt.text.toString())
                    view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.vk_logo)
                }

                telegramEt.id -> {
                    clientsViewModel.startTelegram(telegramEt.text.toString())
                    view.findViewById<ImageView>(R.id.icon)
                        .setImageResource(R.drawable.telegram_logo)

                }

                instagramEt.id -> {
                    clientsViewModel.startInstagram(instagramEt.text.toString())
                    view.findViewById<ImageView>(R.id.icon)
                        .setImageResource(R.drawable.instagram_logo)
                }

                whatsappEt.id -> {
                    clientsViewModel.startWhatsApp(whatsappEt.text.toString())
                    view.findViewById<ImageView>(R.id.icon)
                        .setImageResource(R.drawable.whatsapp_logo)
                }
            }
        }

        edit.setOnClickListener {
            // Edit
            clickedView.isFocusableInTouchMode = true
            clickedView.requestFocus()

            Util().showKeyboard(requireContext())

            // убираем клик листенер, чтобы он бесконечно не выскакивал
            // потом вернем его в событии onEditorActionListener или onFocusChangeListener
            clickedView.setOnClickListener(null)

            // устанавливаем курсор в конец поля
            clickedView.setSelection(clickedView.text.length)

            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun onEditorActionListener(i: Int, et: EditText): Boolean {
        // установки слушателя событий действий редактора ввода
        if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NONE) {
            et.clearFocus() // снять фокус с редактированного поля
            et.isFocusableInTouchMode = false // запретить редактирование поля по нажатию

            // установить клик листенер на поле
            et.setOnClickListener {
                showOptionsDialog(et)
            }
        }
        return false
    }

    private fun onFocusChangeListener(hasFocus: Boolean, et: EditText) {
        // установки слушателя событий если фокус снят
        if (!hasFocus) {
            et.clearFocus() // снять фокус с редактированного поля
            et.isFocusableInTouchMode = false

            et.setOnClickListener {
                showOptionsDialog(et)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // TODO: переписать с деприкейтед метода
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode != REQUEST_CONTACT_PICK) {
            val uri = data?.data

            // set temp photo into view
            clientPhoto.setImageURI(uri)

            // set temp photo path
            tempPhotoFile = uri!!.path?.let { File(it) }!!
        }

        if (requestCode == REQUEST_CONTACT_PICK && resultCode == Activity.RESULT_OK) {
            val contactUri: Uri = data?.data ?: return
            val projection: Array<String> = arrayOf(ContactsContract.Contacts._ID)

            // Получаем ID контакта
            requireActivity().contentResolver.query(contactUri, projection, null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val contactId =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        getPhoneNumber(contactId)
                    }
                }
        }
    }

    private fun deleteTempFolder() {
        val folderToDelete = File(requireContext().filesDir, "ClientAvatarTemp")
        if (folderToDelete.exists()) {
            folderToDelete.deleteRecursively()
        }
    }

    private fun copyPhotoIntoClientFolder() {
        val destinationDir =
            File(requireContext().filesDir, "ClientFiles/${clientsViewModel.selectedClient?._id}")

        // create dir
        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        val destinationFile = File(destinationDir, tempPhotoFile!!.name)

        try {
            tempPhotoFile!!.copyTo(destinationFile, true)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        deleteFilesExceptOne(destinationDir, destinationFile)

        setPhotoToSelectedClient(destinationFile)
    }

    private fun setDataIntoSelectedClient() {
        // put data from edit text into selected client
        clientsViewModel.selectedClient = clientsViewModel.selectedClient?.copy(
            _id = clientsViewModel.selectedClient?._id,
            name = nameEt.text.toString(),
            phone = phoneEt.text.toString(),
            vk = vkEt.text.toString(),
            telegram = telegramEt.text.toString(),
            instagram = instagramEt.text.toString(),
            whatsapp = whatsappEt.text.toString(),
            notes = notesEt.text.toString()
        )
    }

    private fun setNewClientId(id: Long) {
        clientsViewModel.selectedClient = clientsViewModel.selectedClient?.copy(_id = id)
    }

    private fun showToastSaved() {
        Toast.makeText(
            context,
            getString(R.string.client_created, clientsViewModel.selectedClient?.name),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun setPhotoToSelectedClient(newPhotoFile: File) {
        clientsViewModel.selectedClient =
            clientsViewModel.selectedClient?.copy(photo = newPhotoFile.path)
    }

    private suspend fun updateAppointmentListInSelectedDate() {
        dateParamsViewModel.selectedDate.value?.appointmentsList =
            dateParamsViewModel.getArrayOfAppointments(date = dateParamsViewModel.selectedDate.value?.date!!)
    }

    private fun deleteFilesExceptOne(folderPath: File, fileToKeep: File) {
        val files = folderPath.listFiles()
        files?.forEach { file ->
            if (file != fileToKeep) {
                file.delete()
            }
        }
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Запрос разрешения у пользователя
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS
            )
        } else {
            // Разрешение уже предоставлено, можем запускать выбор контакта
            pickContact()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_READ_CONTACTS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Разрешение предоставлено
                pickContact()
            } else {
                // Разрешение отклонено
            }
        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, REQUEST_CONTACT_PICK)
    }

    private fun getPhoneNumber(contactId: String) {
        val phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection: Array<String> = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId)

        requireActivity().contentResolver.query(
            phoneUri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val phoneNumber =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneEt.setText(phoneNumber)
                Toast.makeText(
                    requireContext(),
                    "Номер телефона добавлен \n $phoneNumber",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}