package com.epam.esm.gym.dao;

import com.epam.esm.gym.domain.Trainer;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JDBCTrainerDao implements TrainerDao {

    private final SessionFactory factory;

    public JDBCTrainerDao(SessionFactory sessionFactory) {
        this.factory = sessionFactory;
    }

    @Override
    public List<Trainer> findAll() {
        return null;
    }

    @Override
    public Optional<Trainer> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Trainer save(Trainer trainer) {
        return null;
    }

    @Override
    public void update(Trainer entity) {

    }

    @Override
    public void delete(Trainer trainer) {

    }

    @Override
    public void activateTrainer(Long id) {

    }

    @Override
    public void updateTrainer(Trainer trainer) {

    }
}
