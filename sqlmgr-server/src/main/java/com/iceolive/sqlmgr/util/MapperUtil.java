package com.iceolive.sqlmgr.util;


import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangmianzhe
 */
public class MapperUtil {
    public static <D> D map(Object source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(source, destinationType);
    }

    public static <D> void map(Object source, D destination) {
        if (source == null) {
            destination = null;
        } else {
            ModelMapper modelMapper = new ModelMapper();
            modelMapper.map(source, destination);
        }
    }
    public static <D, S> List<D> map(List<S> source, Class<D> destinationType) {
        if (source == null) {
            return null;
        }
        List<D> result = new ArrayList<D>();
        for (S item : source) {
            result.add(map(item, destinationType));
        }
        return result;
    }
}