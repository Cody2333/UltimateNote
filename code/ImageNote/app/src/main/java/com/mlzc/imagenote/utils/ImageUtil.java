package com.mlzc.imagenote.utils;

import android.net.Uri;


public class ImageUtil {

    public static Uri getAvatarUrl(String email, int size) {
        return Gravatar
                .init(email)
                .ssl()
                .size(size)
                .defaultImage(Gravatar.DefaultImage.IDENTICON)
                .build();
    }
}
