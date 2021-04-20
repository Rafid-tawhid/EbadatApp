

package com.abc.sharefilesz.object;

import androidx.annotation.Nullable;

import com.abc.sharefilesz.io.Containable;

public interface Container
{
    @Nullable
    Containable expand();
}