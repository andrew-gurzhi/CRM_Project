package com.ewp.crm.service.interfaces;


import com.ewp.crm.models.Role;
import com.ewp.crm.models.User;
import com.ewp.crm.models.UserRoutes;
import com.ewp.crm.models.dto.MentorDtoForMentorsPage;
import com.ewp.crm.models.dto.UserRoutesDto;
import com.ewp.crm.models.dto.UserDtoForBoard;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface UserService extends CommonService<User> {

	Optional<User> getByEmailOrPhone(String email, String phone);

	@Override
    User add(User user);

	@Override
	void update(User user);

	@Override
	void delete(User user);

	@Override
	void delete(Long user_id);

	void addPhoto(MultipartFile file, User user);

	List<User> getByRole(Role role);

	Optional<User> getUserByEmail(String email);

	Optional<User> getUserByFirstNameAndLastName(String firstName, String lastName);

	void setColorBackground(String color, User user);

	List<User> getUserByVkToken(long id);

	Optional<User> getUserToOwnCard(UserRoutes.UserRouteType routetype);

	Optional<User> getUserToOwnCard();

	List<MentorDtoForMentorsPage> getAllMentors();

	Optional<List<UserDtoForBoard>> getAllMentorsForDto();

	Optional<List<UserDtoForBoard>> getAllWithoutMentorsForDto();

}
