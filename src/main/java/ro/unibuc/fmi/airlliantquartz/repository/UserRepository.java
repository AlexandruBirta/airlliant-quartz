package ro.unibuc.fmi.airlliantquartz.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.fmi.airlliantmodel.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    default User findByIdAndLock(Long id, EntityManager entityManager) {

        TypedQuery<User> query = entityManager.createNamedQuery("User.findByIdAndLock", User.class);

        query.setParameter("id", id);

        return query.getSingleResult();

    }

}