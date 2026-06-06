package vn.edu.hcmute.minlish.data.util

import java.util.Calendar

object TimeProvider {
    // Độ lệch số ngày giả lập (0 có nghĩa là thời gian thực tế)
    var simulatedDaysOffset: Int = 0

    fun currentTimeMillis(): Long {
        if (simulatedDaysOffset == 0) {
            return System.currentTimeMillis()
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, simulatedDaysOffset)
        return calendar.timeInMillis
    }
}
