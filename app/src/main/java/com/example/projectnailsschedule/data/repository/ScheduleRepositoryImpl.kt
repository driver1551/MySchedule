package com.example.projectnailsschedule.data.repository

import android.content.Context
import com.example.projectnailsschedule.data.storage.ScheduleDb
import com.example.projectnailsschedule.domain.models.AppointmentModelDb
import com.example.projectnailsschedule.domain.models.ClientModelDb
import com.example.projectnailsschedule.domain.repository.repo.ScheduleRepository
import com.example.projectnailsschedule.utils.Util
import java.time.LocalDate

class ScheduleRepositoryImpl(context: Context) : ScheduleRepository {
    private var dao = ScheduleDb.getDb(context).getDao()

    override suspend fun insertAppointment(appointmentModelDb: AppointmentModelDb): Long {
        return dao.insert(appointmentModelDb = appointmentModelDb)
    }

    override suspend fun updateAppointment(appointmentModelDb: AppointmentModelDb): Boolean {
        dao.update(appointmentModelDb = appointmentModelDb)
        return true
    }

    override suspend fun deleteAppointment(appointmentModelDb: AppointmentModelDb): Boolean {
        dao.delete(appointmentModelDb = appointmentModelDb)
        return true
    }

    override suspend fun searchAppointment(searchQuery: String): MutableList<AppointmentModelDb> {
        return dao.searchAppointment(searchQuery = searchQuery)
    }

    override suspend fun getDateAppointments(date: LocalDate): MutableList<AppointmentModelDb> {
        return dao.getDateAppointments(Util().dateConverterNew(date.toString()))
    }

    override suspend fun updateClientInAppointments(clientModelDb: ClientModelDb) {
        with(clientModelDb) {
            dao.updateClientInAppointments(
                _id!!, name, phone, vk, telegram, instagram, whatsapp, notes, photo
            )
        }
    }

    override suspend fun getAll(): List<AppointmentModelDb> {
        return dao.getAll()
    }

    override suspend fun getById(id: Long): AppointmentModelDb? {
        return dao.getById(id)
    }

    override suspend fun getNotSyncAppointments(): List<AppointmentModelDb> {
        return dao.getNotSyncAppointments()
    }

    override suspend fun getDeletedAppointments(): List<AppointmentModelDb> {
        return dao.getDeletedAppointments()
    }

    override suspend fun getMaxTimestamp(): Long? {
        return dao.getMaxAppointmentTimestamp()
    }

    override suspend fun getBySyncUUID(uuid: String): AppointmentModelDb? {
        return dao.getBySyncUUID(uuid)
    }

    override suspend fun getCount(): Long {
        return dao.getCount()
    }

    override suspend fun getOldUpdatedAppointments(): List<AppointmentModelDb> {
        return dao.getOldUpdatedAppointments()
    }
}