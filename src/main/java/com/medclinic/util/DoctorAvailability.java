package com.medclinic.util;

import com.medclinic.entity.DoctorProfile;

public final class DoctorAvailability {
    private DoctorAvailability() { }

    public static boolean acceptsBookings(DoctorProfile doctor) {
        return doctor != null && doctor.getUser() != null
                && Boolean.TRUE.equals(doctor.getUser().getIsActive())
                && doctor.getSpecialty() != null
                && Boolean.TRUE.equals(doctor.getSpecialty().getIsActive());
    }
}
