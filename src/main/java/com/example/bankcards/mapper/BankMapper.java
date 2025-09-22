package com.example.bankcards.util;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BankMapper {

    DateTimeFormatter MMYY = DateTimeFormatter.ofPattern("MM/yy");

    @Mapping(target = "cardNumber",
            expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    @Mapping(target = "status", expression = "java(card.getStatus().name())")
    @Mapping(target = "expiration",
            expression = "java(card.getExpiration().format(MMYY))")
    CardDto cardToCardDto(Card card);


    UserDto userToUserDTO(User user);
    User userCreateDtoToUser(UserCreateDto userDto);
}
