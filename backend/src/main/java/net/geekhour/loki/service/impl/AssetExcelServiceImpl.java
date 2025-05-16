package net.geekhour.loki.service.impl;

import com.alibaba.excel.EasyExcel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.geekhour.loki.entity.Asset;
import net.geekhour.loki.entity.dto.AssetDTO;
import net.geekhour.loki.mapper.AssetMapper;
import net.geekhour.loki.mapper.AssetTypeMapper;
import net.geekhour.loki.mapper.DepartmentMapper;
import net.geekhour.loki.mapper.UserMapper;

import java.io.IOException;  // 正确的 IOException
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class AssetExcelServiceImpl {

    @Autowired
    private AssetMapper assetMapper;
    @Autowired
    private AssetTypeMapper assetTypeMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private UserMapper userMapper; // 用于 ownerName / userName → ID

    public boolean importData(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            List<AssetDTO> dataList = EasyExcel.read(in)
                    .head(AssetDTO.class)
                    .sheet()
                    .doReadSync();

            for (AssetDTO dto : dataList) {
                Asset asset = convertToAsset(dto);
                if (asset == null) {
                    // 关联数据查不到，跳过
                    continue;
                }
                // 唯一性校验：编号不存在才插入
                if (!assetMapper.checkAssetCodeExists(dto.getCode())) {
                    assetMapper.insert(asset);
                }
            }
            return true;

        } catch (IOException e) {
            throw new RuntimeException("文件读取失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("数据处理错误: " + e.getMessage(), e);
        }
    }

    private Asset convertToAsset(AssetDTO dto) {
        // —— 1. 名称 → ID 查表 ——
        Long typeId  = assetTypeMapper.getIdByName(dto.getType());
        Long deptId  = departmentMapper.getDepartmentIdByName(dto.getDepartmentName());
        Long ownerId = userMapper.getIdByUsername(dto.getOwnerName());
        Long userId  = userMapper.getIdByUsername(dto.getUserName());
        if (typeId == null || deptId == null || ownerId == null || userId == null) {
            // 关联数据不齐全，跳过或抛异常
            return null;
        }

        Asset asset = new Asset();
        // 如果是更新场景，设置 ID
        if (dto.getId() != null && !dto.getId().isBlank()) {
            asset.setId(Long.valueOf(dto.getId()));
        }

        // —— 2. 直接字段映射 ——
        asset
                .setName(dto.getName())
                .setCode(dto.getCode())
                .setSn(dto.getSn())
                .setType(typeId)
                .setModel(dto.getModel())
                .setConfig(dto.getConfig())
                .setIp(dto.getIp())
                .setDescription(dto.getDescription())
                .setProvider(dto.getProvider())
                .setDepartmentId(deptId)
                .setOwnerId(ownerId)
                .setUserId(userId)
                .setLocation(dto.getLocation())
                .setStatus(dto.getStatus())
                .setUseStatus(dto.getUseStatus());

        // —— 3. 日期 & 财务 & 数量类型转换 ——
        if (dto.getPurchaseDate() != null && !dto.getPurchaseDate().isBlank()) {
            LocalDate ld = LocalDate.parse(dto.getPurchaseDate());  // 格式 YYYY-MM-DD
            long millis = ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            asset.setPurchaseDate(millis); // 之后在数据库里存的是一个标准的时间戳
        }
        asset
                .setPurchasePrice(dto.getPurchasePrice())
                .setCount(dto.getCount());

        return asset;
    }

}
