package com.example.hopitalapp.dao.interfaces;

import com.example.hopitalapp.model.Departement;
import java.util.List;

public interface DepartementDAO {
    Departement findById(int id);
    List<Departement> findAll();
    void save(Departement departement);
    void update(Departement departement);
    void delete(int id);
    long count();
}
