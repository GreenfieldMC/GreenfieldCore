package net.greenfieldmc.core.codes.services;

import net.greenfieldmc.core.IModuleService;
import net.greenfieldmc.core.codes.Code;

import java.util.List;

public interface ICodesService extends IModuleService<ICodesService> {

    void addCode(String code);

    void removeCode(int id);

    Code getCode(int id);

    List<Code> getCodes();

    void reload();

    void save();

}
