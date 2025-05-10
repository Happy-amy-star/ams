package net.geekhour.loki.service.impl;

import net.geekhour.loki.common.StringUtil;
import net.geekhour.loki.entity.Asset;
import net.geekhour.loki.entity.Department;
import net.geekhour.loki.entity.dto.AssetDTO;
import net.geekhour.loki.mapper.AssetMapper;
import net.geekhour.loki.mapper.AssetTypeMapper;
import net.geekhour.loki.mapper.DepartmentMapper;
import net.geekhour.loki.service.IAssetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Jasper Yang
 * @since 2025-04-05
 */
@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements IAssetService {

    @Autowired
    AssetMapper assetMapper;

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    AssetTypeMapper assetTypeMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<Asset> all() {
        return assetMapper.selectList(null);
    }

    @Override
    public List<AssetDTO> getAssetList(Integer offset, Integer size, String name) {
        return assetMapper.getAssetList(offset, size, name);
    }

    @Override
    public Long countAssets(String name) {
        return assetMapper.countAssets(name);
    }

    @Override
    @Transactional
    public boolean deleteAsset(Long id) {
        return assetMapper.deleteById(id) > 0;
    }

    @Override
    public boolean checkAssetCodeExists(String assetCode) {
        return assetMapper.checkAssetCodeExists(assetCode);
    }

    @Override
    public boolean checkAssetNameExists(String assetName) {
        return assetMapper.checkAssetNameExists(assetName);
    }

    @Override
    @Transactional
    public boolean createAsset(AssetDTO assetDTO) {
        if (StringUtil.isEmpty(assetDTO.getAssetName()) || StringUtil.isEmpty(assetDTO.getAssetCode())) {
            return false;
        }
        if (assetMapper.checkAssetCodeExists(assetDTO.getAssetCode())) {
            return false; // Asset code must be unique
        }
        if (assetMapper.checkAssetNameExists(assetDTO.getAssetName())) {
            return false; // Asset name must be unique
        }

        Asset asset = mapToAsset(assetDTO);
        if (asset == null) {
            return false;
        }
        return assetMapper.insert(asset) > 0;
    }

    @Override
    @Transactional
    public boolean updateAsset(AssetDTO assetDTO) {
        if (assetDTO.getId() == null || assetDTO.getId().isEmpty()) {
            return false;
        }

        Asset existingAsset = assetMapper.selectById(assetDTO.getId());
        if (existingAsset == null) {
            return false; // Asset must exist for update
        }

        // Check for duplicate code, excluding the current asset
        if (!existingAsset.getAssetCode().equals(assetDTO.getAssetCode()) &&
                assetMapper.checkAssetCodeExists(assetDTO.getAssetCode())) {
            return false;
        }

        // Check for duplicate name, excluding the current asset
        if (!existingAsset.getAssetName().equals(assetDTO.getAssetName()) &&
                assetMapper.checkAssetNameExists(assetDTO.getAssetName())) {
            return false;
        }

        Asset asset = mapToAsset(assetDTO);
        if (asset == null) {
            return false;
        }

        return assetMapper.updateById(asset) > 0;
    }

    private Asset mapToAsset(AssetDTO assetDTO) {
        Long assetTypeId = assetTypeMapper.getIdByName(assetDTO.getAssetType());
        if (assetTypeId == null) {
            return null;
        }
        Long departmentId = departmentMapper.getDepartmentIdByName(assetDTO.getDepartmentName());
        if (departmentId == null) {
            return null;
        }

        Asset asset = new Asset();
        asset.setId(Long.valueOf(assetDTO.getId()));
        asset.setAssetName(assetDTO.getAssetName());
        asset.setAssetCode(assetDTO.getAssetCode());
        asset.setAssetType(assetTypeId);
        asset.setDepartmentId(departmentId);
        asset.setLocation(assetDTO.getLocation());
        asset.setStatus(assetDTO.getStatus());

        try {
            Long purchaseDateTimestamp = LocalDate.parse(assetDTO.getPurchaseDate(), DATE_FORMATTER)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            asset.setPurchaseDate(purchaseDateTimestamp);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid purchase date format. Expected format: yyyy-MM-dd");
        }

        asset.setPurchasePrice(assetDTO.getPurchasePrice());
        asset.setCount(assetDTO.getCount());
        return asset;
    }
}
