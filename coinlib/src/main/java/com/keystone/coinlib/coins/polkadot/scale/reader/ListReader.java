package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;

import java.util.ArrayList;
import java.util.List;

public class ListReader<T> implements ScaleReader<List<T>> {

    private ScaleReader<T> scaleReader;

    public ListReader(ScaleReader<T> scaleReader) {
        this.scaleReader = scaleReader;
    }

    @Override
    public List<T> read(ScaleCodecReader rdr) {
        int size = rdr.readCompactInt();
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(rdr.read(scaleReader));
        }
        return result;
    }
}
