package com.ThreeK_Project.api_server.domain.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {

    // 가게 이름
    private String name;
    // 가게 주소
    private String address;
    // 가게 전화번호
    private String phoneNumber;
    // 가게 설명
    private String description;
    // 가게 위치 id
    private int locationId;
    // 가게 카테고리 id
    private int categoryId;
}
