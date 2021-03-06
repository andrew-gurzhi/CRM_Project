package com.ewp.crm.repository.interfaces;

import com.ewp.crm.models.UserRoutes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface UserRoutesRepository  extends JpaRepository<UserRoutes, Long>, UserRoutesRepositoryCustom {
    List<UserRoutes> getAllByUserRouteType(UserRoutes.UserRouteType userRouteType);
    Set<UserRoutes> getByUserId(Long userId);
    UserRoutes getByUserIdAndUserRouteType(Long userId, UserRoutes.UserRouteType type);
}
