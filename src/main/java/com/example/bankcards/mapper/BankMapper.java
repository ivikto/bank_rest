package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface BankMapper {

    DateTimeFormatter MMYY = DateTimeFormatter.ofPattern("MM/yy");

    @Mapping(target = "cardNumber",
            expression = "java(\"**** **** **** \" + card.getCardNumberLast4())")
    @Mapping(target = "status", expression = "java(card.getCardStatus().name())")
    @Mapping(target = "expiration",
            expression = "java(card.getExpiration().format(MMYY))")
    CardDto cardToCardDto(BaseCard card);

    @AfterMapping
    default void fillEmptyCards(BaseUser src, @MappingTarget UserDto dto) {
        if (dto.cards == null) dto.cards = java.util.Collections.emptyList();
    }


    UserDto userToUserDTO(BaseUser user);
    StandardUser userCreateDtoToUser(UserCreateDto userDto);
}
