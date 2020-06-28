package com.speakerz.model.network.event;

import android.Manifest;

import com.speakerz.model.enums.PERM;
import com.speakerz.util.EventArgs;

public class PermissionCheckEventArgs extends EventArgs {


    public String getRequiredPermission() {
        return requiredPermission;
    }

    public Integer getSuccessNumber() {
        return successNumber;
    }

    PERM permissionType;
    String requiredPermission;
    Integer successNumber;

    public PermissionCheckEventArgs(Object _sender, PERM permissionType, String requiredPermission, Integer successIdentifier) {
        super(_sender);
        this.permissionType = permissionType;
        this.requiredPermission = requiredPermission;
        this.successNumber = successIdentifier;
    }

    public PERM getReason() {
        return permissionType;
    }


}
