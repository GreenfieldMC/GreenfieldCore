package com.njdaeger.greenfieldcore.codes.services;

import com.njdaeger.greenfieldcore.IModuleService;
import com.njdaeger.greenfieldcore.codes.Code;

import java.util.List;

public interface ICodesService extends IModuleService<ICodesService> {

    void addCode(String code);

    void removeCode(int id);

    Code getCode(int id);

    List<Code> getCodes();

    void reload();

    void save();

}
