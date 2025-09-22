package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.UserCreateDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.BaseCard;
import com.example.bankcards.entity.BaseUser;
import com.example.bankcards.entity.StandardUser;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-22T11:19:59+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 23-valhalla (Oracle Corporation)"
)
@Component
public class BankMapperImpl implements BankMapper {

    @Override
    public CardDto cardToCardDto(BaseCard card) {
        if ( card == null ) {
            return null;
        }

        Long id = null;
        BigDecimal balance = null;

        id = card.getId();
        balance = card.getBalance();

        String cardNumber = "**** **** **** " + card.getCardNumberLast4();
        String status = card.getCardStatus().name();
        String expiration = card.getExpiration().format(MMYY);

        CardDto cardDto = new CardDto( id, cardNumber, status, balance, expiration );

        return cardDto;
    }

    @Override
    public UserDto userToUserDTO(BaseUser user) {
        if ( user == null ) {
            return null;
        }

        UserDto userDto = new UserDto();

        userDto.id = user.getId();
        userDto.name = user.getName();
        userDto.email = user.getEmail();
        userDto.cards = baseCardListToCardDtoList( user.cards );
        userDto.role = user.role;
        userDto.isActive = user.isActive;
        userDto.createdAt = user.createdAt;

        fillEmptyCards( user, userDto );

        return userDto;
    }

    @Override
    public StandardUser userCreateDtoToUser(UserCreateDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        StandardUser standardUser = new StandardUser();

        standardUser.role = userDto.role();
        standardUser.name = userDto.name();
        standardUser.email = userDto.email();

        return standardUser;
    }

    protected List<CardDto> baseCardListToCardDtoList(List<BaseCard> list) {
        if ( list == null ) {
            return null;
        }

        List<CardDto> list1 = new ArrayList<CardDto>( list.size() );
        for ( BaseCard baseCard : list ) {
            list1.add( cardToCardDto( baseCard ) );
        }

        return list1;
    }
}
