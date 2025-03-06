package com.njdaeger.greenfieldcore.services;

import com.njdaeger.greenfieldcore.IModuleService;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.List;

public interface ILuckPermsService extends IModuleService<ILuckPermsService> {

    List<Group> getAllGroups();

    List<User> getUsersInGroup(Group group);



}
