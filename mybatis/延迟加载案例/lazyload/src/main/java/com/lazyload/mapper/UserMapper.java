package com.lazyload.mapper;

import com.lazyload.pojo.Account;
import com.lazyload.pojo.User;

import java.util.List;

public interface UserMapper {

    List<User> findAllby();

    List<Account> findAllByUid(Integer uid);
}
