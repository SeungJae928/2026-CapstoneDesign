package com.example.Capstone.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.Capstone.domain.ListRestaurant;
import com.example.Capstone.domain.Restaurant;
import com.example.Capstone.domain.User;
import com.example.Capstone.domain.UserList;
import com.example.Capstone.dto.request.AddRestaurantRequest;
import com.example.Capstone.exception.BusinessException;
import com.example.Capstone.repository.ListRestaurantRepository;
import com.example.Capstone.repository.RestaurantRepository;
import com.example.Capstone.repository.UserListRepository;
import com.example.Capstone.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserListServiceRegionMatchTest {

    @Mock
    private UserListRepository userListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ListRestaurantRepository listRestaurantRepository;

    @InjectMocks
    private UserListService userListService;

    @Test
    @DisplayName("리스트 지역과 식당 지역이 exact match가 아니면 추가에 실패한다")
    void addRestaurantFailsWhenRegionDoesNotExactlyMatch() {
        Long userId = 1L;
        Long listId = 10L;
        Long restaurantId = 100L;
        UserList userList = ownedList(userId, listId, "서울");
        Restaurant restaurant = restaurant(restaurantId, "서울 강남");

        when(userListRepository.findByIdAndIsDeletedFalse(listId)).thenReturn(Optional.of(userList));
        when(listRestaurantRepository.findByUserListIdAndRestaurantId(listId, restaurantId))
                .thenReturn(Optional.empty());
        when(restaurantRepository.findByIdAndIsDeletedFalseAndIsHiddenFalse(restaurantId))
                .thenReturn(Optional.of(restaurant));

        assertThrows(BusinessException.class, () -> userListService.addRestaurant(
                userId,
                listId,
                addRestaurantRequest(restaurantId)
        ));

        verify(listRestaurantRepository, never()).save(org.mockito.ArgumentMatchers.any(ListRestaurant.class));
    }

    private AddRestaurantRequest addRestaurantRequest(Long restaurantId) {
        return new AddRestaurantRequest(
                restaurantId,
                new BigDecimal("8.0"),
                new BigDecimal("7.0"),
                new BigDecimal("6.0")
        );
    }

    private UserList ownedList(Long userId, Long listId, String regionName) {
        User user = User.builder()
                .provider("kakao")
                .providerUserId("provider-user")
                .nickname("tester")
                .profileImageUrl("profile")
                .role(User.Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        UserList userList = UserList.builder()
                .user(user)
                .title("리스트")
                .description("설명")
                .regionName(regionName)
                .build();
        ReflectionTestUtils.setField(userList, "id", listId);
        return userList;
    }

    private Restaurant restaurant(Long restaurantId, String regionName) {
        Restaurant restaurant = Restaurant.builder()
                .name("식당")
                .address("서울시")
                .regionName(regionName)
                .lat(new BigDecimal("37.0"))
                .lng(new BigDecimal("127.0"))
                .imageUrl("image")
                .build();
        ReflectionTestUtils.setField(restaurant, "id", restaurantId);
        return restaurant;
    }
}
