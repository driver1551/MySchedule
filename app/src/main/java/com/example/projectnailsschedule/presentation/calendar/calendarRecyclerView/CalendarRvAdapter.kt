package com.example.projectnailsschedule.presentation.calendar.calendarRecyclerView

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.projectnailsschedule.R
import com.example.projectnailsschedule.domain.models.CalendarDateModelDb
import com.example.projectnailsschedule.domain.models.DateParams
import com.example.projectnailsschedule.presentation.calendar.DateParamsViewModel
import com.example.projectnailsschedule.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class CalendarRvAdapter(
    private val daysInMonth: ArrayList<String>,
    private val dateParamsViewModel: DateParamsViewModel,
    private val context: Context
) : RecyclerView.Adapter<CalendarRvAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        lateinit var date: TextView
        lateinit var dateAppointmentsCount: TextView
        lateinit var cellLayout: ConstraintLayout
        lateinit var dateIcon: ImageView
        lateinit var selectedBackground: ImageView
        lateinit var selectedBackgroundRed: ImageView
        lateinit var currentDate: ImageView
        lateinit var progressBar: ProgressBar

        init {
            inflateViews()
        }

        private fun inflateViews() {
            date = itemView.findViewById(R.id.date_cell)
            dateAppointmentsCount = itemView.findViewById(R.id.date_appointments_text_view)
            cellLayout = itemView.findViewById(R.id.calendarRecyclerViewCell)
            dateIcon = itemView.findViewById(R.id.day_off_icon)
            selectedBackground = itemView.findViewById(R.id.selected_background)
            selectedBackgroundRed = itemView.findViewById(R.id.selected_background_red)
            currentDate = itemView.findViewById(R.id.current_date)
        }
    }

    private val log = this::class.simpleName

    private lateinit var greenImageView: ImageView
    private lateinit var yellowImageView: ImageView
    private lateinit var redImageView: ImageView
    private lateinit var blueImageView: ImageView
    private lateinit var resetImageView: ImageView
    private var holders: MutableList<ViewHolder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.calendar_rv_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // get day to work with
        val dayInHolder = daysInMonth[position]

        // set day number in CalendarViewHolder
        holder.date.text = dayInHolder

        setCellBackground(position, holder)

        // set appointments count
        if (dayInHolder != "") {
            // get appointment count from date
            val selectedDate = DateParams(
                date = dateParamsViewModel.selectedDate.value?.date?.withDayOfMonth(
                    dayInHolder.toInt()
                )
            )
            val ruFormatDate = Util().formatDateToRus(selectedDate.date!!)

            // get date appointments
            CoroutineScope(Dispatchers.IO).launch {
                selectedDate.appointmentsList =
                    dateParamsViewModel.getArrayOfAppointments(date = selectedDate.date!!)
                withContext(Dispatchers.Main) {
                    // set appointments size into holder
                    if (selectedDate.appointmentsList!!.isNotEmpty()) {
                        holder.dateAppointmentsCount.text =
                            selectedDate.appointmentsList!!.size.toString()
                    }
                }
            }

            // get date color from database
            CoroutineScope(Dispatchers.IO).launch {
                val dateColor = dateParamsViewModel.getDateColor(ruFormatDate)
                withContext(Dispatchers.Main) {
                    if (dateColor != null) {
                        // set color to holder
                        holder.cellLayout.setBackgroundResource(mapColorToDrawable(dateColor))
                    }
                }
            }

            setOnCalendarClickListener(holder, selectedDate)

            setOnCalendarLongClickListener(holder, ruFormatDate)

            CoroutineScope(Dispatchers.IO).launch {
                workWithProductionCalendar(ruFormatDate, holder)
            }

            markCurrentDate(holder, selectedDate)

            restoreSelection(holder)
        }
    }

    override fun getItemCount(): Int {
        return daysInMonth.size
    }

    private fun setDateColorDialog(holder: ViewHolder, ruFormatDate: String) {
        // Start scale up animation
        val scaleUpAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up)
        holder.cellLayout.startAnimation(scaleUpAnimation)

        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        builder.setView(dialogView)

        // Add date to the dialog title
        val dateColorDialogTitle = dialogView.findViewById<TextView>(R.id.DateColorDialogTitle)
        dateColorDialogTitle.text =
            context.getString(R.string.formatted_date, ruFormatDate)

        val dialog = builder.create()
        val layoutParams = WindowManager.LayoutParams()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.window?.attributes = layoutParams

        dialog.show()

        setColorsClickListeners(holder, dialogView, dialog, ruFormatDate)
    }

    private fun setColorsClickListeners(
        holder: ViewHolder,
        dialogView: View,
        dialog: AlertDialog,
        ruFormatDate: String
    ) {
        initColorsImageButtons(dialogView)

        greenImageView.setOnClickListener {
            val color = "green"
            paintViewHolder(holder, color)

            CoroutineScope(Dispatchers.IO).launch {
                changeColorInDb(ruFormatDate = ruFormatDate, color = color)
            }
            dialog.dismiss()
        }

        yellowImageView.setOnClickListener {
            val color = "yellow"
            paintViewHolder(holder, color)

            CoroutineScope(Dispatchers.IO).launch {
                changeColorInDb(ruFormatDate = ruFormatDate, color = color)
            }
            dialog.dismiss()
        }

        redImageView.setOnClickListener {
            val color = "red"
            paintViewHolder(holder, color)

            CoroutineScope(Dispatchers.IO).launch {
                changeColorInDb(ruFormatDate = ruFormatDate, color = color)
            }
            dialog.dismiss()
        }

        blueImageView.setOnClickListener {
            val color = "blue"
            paintViewHolder(holder, color)

            CoroutineScope(Dispatchers.IO).launch {
                changeColorInDb(ruFormatDate = ruFormatDate, color = color)
            }
            dialog.dismiss()
        }

        resetImageView.setOnClickListener {
            val color = "default"
            paintViewHolder(holder, color)

            CoroutineScope(Dispatchers.IO).launch {
                changeColorInDb(ruFormatDate = ruFormatDate, color = color)
            }
            dialog.dismiss()
        }
    }

    private suspend fun changeColorInDb(ruFormatDate: String, color: String) {
        Log.d("Color", "Changing color for $ruFormatDate")
        val id = dateParamsViewModel.getDateId(ruFormatDate = ruFormatDate)

        if (id == null) {
            // if date is not exists in database
            insertCalendarDate(ruFormatDate = ruFormatDate, color = color)
        } else {
            if (color == "default") {
                // if color is deleted - delete from db
                deleteCalendarDate(id = id)
            } else {
                // if date already exists in database
                updateCalendarDate(id = id, ruFormatDate = ruFormatDate, color = color)
            }
        }
    }

    private suspend fun insertCalendarDate(ruFormatDate: String, color: String) {
        Log.d("Color", "Insert color for date $ruFormatDate")
        val time = Date().time

        val calendarDateModelDb = CalendarDateModelDb(
            date = ruFormatDate,
            color = color,
            syncUUID = UUID.randomUUID().toString(),
            syncTimestamp = time,
            syncStatus = "NotSynchronized"
        )
        dateParamsViewModel.insertCalendarDate(calendarDateModelDb)
    }

    private suspend fun updateCalendarDate(id: Int, ruFormatDate: String, color: String) {
        Log.e("Color", "Updating color for date $ruFormatDate")
        val currentData = dateParamsViewModel.getByIdCalendarDateUseCase.execute(id.toLong())

        val time = Date().time

        val newData = currentData?.copy(
            color = color
        )

        if (newData!!.syncUUID != null) {
            newData.syncTimestamp = time
            newData.syncStatus = "NotSynchronized"
        } else {
            newData.syncTimestamp = null
            newData.syncStatus = null
        }

        dateParamsViewModel.updateCalendarDate(newData)
    }

    private suspend fun deleteCalendarDate(id: Int) {
        val currentData = dateParamsViewModel.getByIdCalendarDateUseCase.execute(id.toLong())
        val time = Date().time

        // Если мы обновляем старую запись (созданную до синхронизации -
        // не ставить ей статус и время, чтобы не запушить на сервер, а просто удалить локально
        if (currentData!!.syncUUID != null) {
            currentData.syncTimestamp = time
            currentData.syncStatus = "DELETED"
        } else {
            currentData.syncTimestamp = null
            currentData.syncStatus = null
            dateParamsViewModel.deleteCalendarDateUseCase.execute(currentData)
        }
        dateParamsViewModel.updateCalendarDate(currentData)
    }

    private fun initColorsImageButtons(dialogView: View) {
        greenImageView = dialogView.findViewById(R.id.circleGreen)
        yellowImageView = dialogView.findViewById(R.id.circleYellow)
        redImageView = dialogView.findViewById(R.id.circleRed)
        blueImageView = dialogView.findViewById(R.id.circleBlue)
        resetImageView = dialogView.findViewById(R.id.circleReset)
    }

    private fun paintViewHolder(holder: ViewHolder, color: String) {
        holder.cellLayout.background = AppCompatResources.getDrawable(
            context,
            mapColorToDrawable(color)
        )
    }

    private fun mapColorToDrawable(color: String): Int {
        return when (color) {
            "green" -> R.drawable.calendar_recycler_view_borders_green
            "yellow" -> R.drawable.calendar_recycler_view_borders_yellow
            "red" -> R.drawable.calendar_recycler_view_borders_red
            "blue" -> R.drawable.calendar_recycler_view_borders_blue
            "default" -> R.drawable.calendar_recycler_view_borders
            else -> R.drawable.calendar_recycler_view_borders
        }
    }

    private fun restoreSelection(holder: ViewHolder) {
        if (dateParamsViewModel.dateDetailsVisibility.value!!) {
            val holderDate = holder.date.text
            val prevHolderDate = dateParamsViewModel.prevCalendarRvHolder?.date?.text

            if (holderDate == prevHolderDate) {
                adjustTextSizeWithAnimation(holder.date, true)

                dateParamsViewModel.prevCalendarRvHolder = holder
            }
        }
    }

    private fun setOnCalendarClickListener(holder: ViewHolder, selectedDate: DateParams) {
        holder.cellLayout.setOnClickListener {
            if (holder != dateParamsViewModel.prevCalendarRvHolder) {

                unSelectPreviousHolder()

                selectDate(holder)

                dateParamsViewModel.prevCalendarRvHolder = holder

                dateParamsViewModel.updateSelectedDate(
                    dateParams = selectedDate
                )

                CoroutineScope(Dispatchers.IO).launch {
                    setDateInfo(selectedDate)
                }

                dateParamsViewModel.dateDetailsVisibility.value = true
            }
        }
    }

    private fun setOnCalendarLongClickListener(holder: ViewHolder, ruFormatDate: String) {
        holder.cellLayout.setOnLongClickListener {
            setDateColorDialog(holder, ruFormatDate)
            true
        }
    }

    private fun markCurrentDate(holder: ViewHolder, selectedDate: DateParams) {
        if (selectedDate.date!! == LocalDate.now()) {
            holder.selectedBackground.visibility = View.VISIBLE
        }
    }

    private fun unSelectPreviousHolder() {
        dateParamsViewModel.prevCalendarRvHolder?.let {
            adjustTextSizeWithAnimation(
                it.date,
                false
            )
        }
    }

    private fun selectDate(holder: ViewHolder) {
        adjustTextSizeWithAnimation(holder.date, true)
    }

    private suspend fun workWithProductionCalendar(ruFormatDate: String, holder: ViewHolder) {
        /**  Типы даты:
        1 - Рабочий день
        2 - Выходной день
        3 - Государственный праздник
        4 - Региональный праздник (не работает)
        5 - Сокращенный рабочий день
        6 - Перенесенный выходной день
        7 - Перенесенный рабочий день */

        val dayNum = Util().getDayOfYear(ruFormatDate) - 1 // Получаем порядковый номер дня
        Log.i("productionCalendarAPI", "Получаем информацию по дате $ruFormatDate № $dayNum")

        val dateInfo = dateParamsViewModel.getDataInfo(dayNum)
        Log.i("GetProductionCalendarDateInfoUseCase", "Данные о дате $ruFormatDate получены")
        Log.i("productionCalendarAPI", "Тип дня № $dayNum - ${dateInfo.typeId}")

        withContext(Dispatchers.Main) {
            when (dateInfo.typeId) {
                1 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Рабочий день")
                    holder.dateIcon.visibility = View.INVISIBLE
                    holder.date.setTextColor(context.resources.getColor(R.color.black))
                    holder.date.setTypeface(null, Typeface.NORMAL)
                }

                2 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Выходной день")
                    holder.dateIcon.visibility = View.INVISIBLE
                    holder.date.setTextColor(context.resources.getColor(R.color.red_weekend))
                    holder.date.setTypeface(null, Typeface.BOLD)
                }

                3 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Праздник")
                    holder.dateIcon.visibility = View.VISIBLE
                    // Установить иконку праздника
                    val notes = dateParamsViewModel.getDataInfo(dayNum).note
                    val icon = dateParamsViewModel.getHolidayIcon(notes)
                    holder.dateIcon.setImageResource(icon)
                    holder.date.setTextColor(context.resources.getColor(R.color.red_weekend))
                    holder.date.setTypeface(null, Typeface.BOLD)
                }

                4 -> {
                    Log.i("productionCalendarAPI", "Не реализовано")
                }

                5 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Сокращенный рабочий день")
                    holder.dateIcon.visibility = View.VISIBLE
                    holder.dateIcon.setImageResource(R.drawable.asterisk)
                    holder.date.setTextColor(context.resources.getColor(R.color.black))
                    holder.date.setTypeface(null, Typeface.NORMAL)
                }

                6 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Перенесенный выходной день")
                    holder.dateIcon.visibility = View.VISIBLE
                    holder.dateIcon.setImageResource(R.drawable.asterisk)
                    holder.date.setTextColor(context.resources.getColor(R.color.red_weekend))
                    holder.date.setTypeface(null, Typeface.BOLD)
                }

                7 -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Перенесенный рабочий день")
                    holder.dateIcon.visibility = View.VISIBLE
                    holder.date.setTextColor(context.resources.getColor(R.color.black))
                    holder.date.setTypeface(null, Typeface.NORMAL)
                    holder.dateIcon.setImageResource(R.drawable.asterisk)
                }

                else -> {
                    Log.i("productionCalendarAPI", "${dateInfo.date} - Неизвестный код")
                }
            }
        }
    }

    private suspend fun setDateInfo(selectedDate: DateParams) {
        CoroutineScope(Dispatchers.IO).launch {
            val dayNum =
                Util().getDayOfYear(Util().formatDateToRus(selectedDate.date!!)) - 1
            val dateInfo = dateParamsViewModel.getDataInfo(dayNum)
            when (dateInfo.typeId) {
                3 -> dateParamsViewModel.dateInfo.postValue(dateInfo.note)
                5, 6, 7 -> dateParamsViewModel.dateInfo.postValue(dateInfo.typeText)
                else -> dateParamsViewModel.dateInfo.postValue(null)
            }
        }
    }

    private fun adjustTextSizeWithAnimation(textView: TextView, increase: Boolean) {
        val currentSize = textView.textSize / textView.resources.displayMetrics.scaledDensity
        var newSize = currentSize

        if (increase) {
            newSize += 6f
            textView.setShadowLayer(
                1.5f, // radius
                2f,   // dx (смещение по X)
                2f,   // dy (смещение по Y)
                textView.currentTextColor // цвет тени
            )

        } else {
            newSize -= 6f
            textView.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }

        val animator = ValueAnimator.ofFloat(currentSize, newSize)
        animator.duration = 300 // Длительность анимации в миллисекундах
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, animatedValue)
        }
        animator.start()
    }

    private fun setCellBackground(position: Int, holder: ViewHolder) {
        // Убираем фон для тех дней месяца, которые "по углам" расположены,
        // чтобы они не портили заливку с круглыми углами,
        // потом нужно будет для них отдельные ресурсы сделать
        holders.add(position, holder)

        if (position == 0 || position == 6) {
            holder.cellLayout.background = null
        }

        if (position == 28 || position == 34) {
            holder.cellLayout.background = null
        }

        if (position == 41) {
            holder.cellLayout.background = null
            holders[35].cellLayout.background = null

            holders[28].cellLayout.setBackgroundResource(R.drawable.calendar_recycler_view_borders)
            holders[34].cellLayout.setBackgroundResource(R.drawable.calendar_recycler_view_borders)
        }
    }
}