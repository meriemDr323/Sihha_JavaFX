package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.Hospital;

public interface HospitalDAO {
    Hospital getInfo();
    void saveOrUpdate(Hospital hospital);
}
