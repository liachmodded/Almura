/**
 * This file is part of Almura, All Rights Reserved.
 *
 * Copyright (c) 2014 - 2017 AlmuraDev <http://github.com/AlmuraDev/>
 */
package com.almuradev.almura.extension.item;

public interface IItemStack {    

    boolean isCache();
    
    void markAsCacheStack(boolean value);
}
