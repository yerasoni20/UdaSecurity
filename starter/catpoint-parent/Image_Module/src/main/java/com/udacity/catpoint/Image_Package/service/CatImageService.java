package com.udacity.catpoint.Image_Package.service;

import java.awt.image.BufferedImage;

public interface CatImageService {
    public boolean imageContainsCat(BufferedImage image,float threshhold);
}
