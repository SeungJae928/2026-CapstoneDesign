package com.example.Capstone.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

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
import com.example.Capstone.dto.request.CreateListRequest;
import com.example.Capstone.exception.BusinessException;
import com.example.Capstone.repository.ListRestaurantRepository;
import com.example.Capstone.repository.RestaurantRepository;
import com.example.Capstone.repository.UserListRepository;
import com.example.Capstone.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserListServiceMinimumRestaurantCountTest {

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
    @DisplayName("리스트 생성 시 초기 식당 수가 5개 미만이면 실패한다")
    void createListFailsWhenRestaurantCountIsLessThanFive() {
        CreateListRequest request = new CreateListRequest(
                "주말 리스트",
                "설명",
                "서울",
                List.of(
                        addRestaurantRequest(1L),
                        addRestaurantRequest(2L),
                        addRestaurantRequest(3L),
                        addRestaurantRequest(4L)
                )
        );

        assertThrows(BusinessException.class, () -> userListService.createList(1L, request));

        verify(userRepository, never()).findByIdAndIsDeletedFalse(1L);
        verify(userListRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("리스트에서 식당을 제거해 5개 미만으로 내려가면 실패한다")
    void removeRestaurantFailsWhenListWouldDropBelowFive() {
        Long userId = 1L;
        Long listId = 10L;
        Long restaurantId = 100L;
        UserList userList = ownedList(userId, listId, "서울");

        when(userListRepository.findByIdAndIsDeletedFalse(listId)).thenReturn(java.util.Optional.of(userList));
        when(listRestaurantRepository.countByUserListId(listId)).thenReturn(5L);

        assertThrows(BusinessException.class, () -> userListService.removeRestaurant(userId, listId, restaurantId));

        verify(listRestaurantRepository, never()).delete(org.mockito.ArgumentMatchers.any(ListRestaurant.class));
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
}
