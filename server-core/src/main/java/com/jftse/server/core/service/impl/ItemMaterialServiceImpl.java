package com.jftse.server.core.service.impl;

import com.jftse.emulator.common.utilities.ResourceUtil;
import com.jftse.entities.database.repository.item.ItemMaterialRepository;
import com.jftse.server.core.service.ItemMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ItemMaterialServiceImpl implements ItemMaterialService {
    private final ItemMaterialRepository itemMaterialRepository;

    @Override
    public List<Integer> findAllItemIndexesDB() {
        return itemMaterialRepository.findAllItemIndexes();
    }

    @Override
    public List<Integer> findAllItemIndexes() {
        List<String> names = new ArrayList<>();

        InputStream inputStream = ResourceUtil.getResource("drops.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (reader.ready()) {
                String name = reader.readLine();
                names.add(name);
            }
        } catch (IOException e) {
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }

        List<Integer> itemIndexes = itemMaterialRepository.findAllItemIndexesByNames(names);
        return new ArrayList<>(itemIndexes);
    }
}
