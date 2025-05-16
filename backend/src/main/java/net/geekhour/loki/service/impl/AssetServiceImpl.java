package net.geekhour.loki.service.impl;

import net.geekhour.loki.common.StringUtil;
import net.geekhour.loki.entity.Asset;
import net.geekhour.loki.entity.dto.AssetDTO;
import net.geekhour.loki.entity.dto.AssetSummaryDTO;
import net.geekhour.loki.mapper.AssetMapper;
import net.geekhour.loki.mapper.AssetTypeMapper;
import net.geekhour.loki.mapper.DepartmentMapper;
import net.geekhour.loki.service.IAssetService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import net.geekhour.loki.entity.dto.AssetExportDTO;
import net.geekhour.loki.entity.dto.AssetImportDTO;
import net.geekhour.loki.entity.dto.AssetImportResultDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
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
    public boolean checkAssetSnExists(String sn) {
        return assetMapper.checkAssetSnExists(sn);
    }

    @Override
    public BigDecimal calculateTotalAmount() {
        return assetMapper.calculateTotalAmount();
    }

    @Override
    public AssetSummaryDTO getSummary() {
        return assetMapper.getAssetSummary();
    }

    @Override
    public void exportToExcel(Map<String, Object> params, HttpServletResponse response) {
        try {
            // 只按 name 筛选，其它条件前端可传但 service 暂不使用
            String name = (String) params.get("name");
            List<AssetDTO> list = assetMapper.getAssetList(0, Integer.MAX_VALUE, name);

            // 转 AssetExportDTO 列表
            List<AssetExportDTO> exports = list.stream().map(dto -> {
                AssetExportDTO d = new AssetExportDTO();
                d.setId(dto.getId());
                d.setName(dto.getName());
                d.setCode(dto.getCode());
                d.setSn(dto.getSn());
                d.setType(dto.getType());
                d.setModel(dto.getModel());
                d.setConfig(dto.getConfig());
                d.setIp(dto.getIp());
                d.setDescription(dto.getDescription());
                d.setProvider(dto.getProvider());
                d.setDepartmentName(dto.getDepartmentName());
                d.setOwnerName(dto.getOwnerName());
                d.setUserName(dto.getUserName());
                d.setLocation(dto.getLocation());
                d.setStatus(dto.getStatus());
                d.setUseStatus(dto.getUseStatus());
                d.setPurchaseDate(dto.getPurchaseDate());
                d.setPurchasePrice(dto.getPurchasePrice());
                d.setCount(dto.getCount());
                return d;
            }).toList();

            // 创建 Excel
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("资产列表");
            // 表头
            Row head = sheet.createRow(0);
            String[] headers = {
                    "ID","名称","编号","序列号","类型","型号","配置","IP","描述","供应商",
                    "部门","负责人","使用人","存放地点","状态","使用状态","购入日期","购买价格","数量"
            };
            for (int i = 0; i < headers.length; i++) {
                head.createCell(i).setCellValue(headers[i]);
            }
            // 数据行
            for (int i = 0; i < exports.size(); i++) {
                AssetExportDTO d = exports.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(d.getId());
                row.createCell(1).setCellValue(d.getName());
                row.createCell(2).setCellValue(d.getCode());
                row.createCell(3).setCellValue(d.getSn());
                row.createCell(4).setCellValue(d.getType());
                row.createCell(5).setCellValue(d.getModel());
                row.createCell(6).setCellValue(d.getConfig());
                row.createCell(7).setCellValue(d.getIp());
                row.createCell(8).setCellValue(d.getDescription());
                row.createCell(9).setCellValue(d.getProvider());
                row.createCell(10).setCellValue(d.getDepartmentName());
                row.createCell(11).setCellValue(d.getOwnerName());
                row.createCell(12).setCellValue(d.getUserName());
                row.createCell(13).setCellValue(d.getLocation());
                row.createCell(14).setCellValue(d.getStatus());
                row.createCell(15).setCellValue(d.getUseStatus());
                row.createCell(16).setCellValue(d.getPurchaseDate());
                row.createCell(17).setCellValue(d.getPurchasePrice().doubleValue());
                row.createCell(18).setCellValue(d.getCount());
            }
            // 输出
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=assets.xlsx");
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            throw new RuntimeException("导出资产 Excel 失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public AssetImportResultDTO importFromExcel(MultipartFile file) throws IOException {
        AssetImportResultDTO result = new AssetImportResultDTO();
        List<String> errors = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            int total = sheet.getLastRowNum();
            result.setTotal(total);

            int success = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                try {
                    AssetImportDTO dto = new AssetImportDTO();
                    dto.setName(row.getCell(1).getStringCellValue());
                    dto.setCode(row.getCell(2).getStringCellValue());
                    dto.setSn(row.getCell(3).getStringCellValue());
                    dto.setType(row.getCell(4).getStringCellValue());
                    dto.setModel(row.getCell(5).getStringCellValue());
                    dto.setConfig(row.getCell(6).getStringCellValue());
                    dto.setIp(row.getCell(7).getStringCellValue());
                    dto.setDescription(row.getCell(8).getStringCellValue());
                    dto.setProvider(row.getCell(9).getStringCellValue());
                    dto.setDepartmentName(row.getCell(10).getStringCellValue());
                    dto.setOwnerName(row.getCell(11).getStringCellValue());
                    dto.setUserName(row.getCell(12).getStringCellValue());
                    dto.setLocation(row.getCell(13).getStringCellValue());
                    dto.setStatus(row.getCell(14).getStringCellValue());
                    dto.setUseStatus(row.getCell(15).getStringCellValue());
                    dto.setPurchaseDate(row.getCell(16).getStringCellValue());
                    dto.setPurchasePrice(BigDecimal.valueOf(row.getCell(17).getNumericCellValue()));
                    dto.setCount((int) row.getCell(18).getNumericCellValue());

                    // 校验
                    if (dto.getName().isBlank() || dto.getCode().isBlank()) {
                        throw new IllegalArgumentException("名称/编码不能为空");
                    }
                    if (assetMapper.checkAssetCodeExists(dto.getCode())) {
                        throw new IllegalArgumentException("编码重复");
                    }

                    // 转实体并插入
                    Asset a = mapImportDtoToAsset(dto);
                    assetMapper.insert(a);
                    success++;
                } catch (Exception ex) {
                    errors.add("第" + (i+1) + "行：" + ex.getMessage());
                }
            }
            result.setSuccessCount(success);
            result.setFailureCount(errors.size());
            result.setErrors(errors);
            return result;
        }
    }

    /** 复用已有 mapToAsset(AssetDTO) 逻辑，通过临时转换 */
    private Asset mapImportDtoToAsset(AssetImportDTO dto) {
        AssetDTO tmp = new AssetDTO();
        // 同 export 映射字段……
        tmp.setName(dto.getName());
        tmp.setCode(dto.getCode());
        tmp.setSn(dto.getSn());
        tmp.setType(dto.getType());
        tmp.setModel(dto.getModel());
        tmp.setConfig(dto.getConfig());
        tmp.setIp(dto.getIp());
        tmp.setDescription(dto.getDescription());
        tmp.setProvider(dto.getProvider());
        tmp.setDepartmentName(dto.getDepartmentName());
        tmp.setOwnerName(dto.getOwnerName());
        tmp.setUserName(dto.getUserName());
        tmp.setLocation(dto.getLocation());
        tmp.setStatus(dto.getStatus());
        tmp.setUseStatus(dto.getUseStatus());
        tmp.setPurchaseDate(dto.getPurchaseDate());
        tmp.setPurchasePrice(dto.getPurchasePrice());
        tmp.setCount(dto.getCount());
        return this.mapToAsset(tmp);  // 调用现有的 mapToAsset(AssetDTO) 方法
    }



    @Override
    @Transactional
    public boolean createAsset(AssetDTO assetDTO) {
        if (StringUtil.isEmpty(assetDTO.getName()) || StringUtil.isEmpty(assetDTO.getCode())) {
            return false;
        }
        if (assetMapper.checkAssetCodeExists(assetDTO.getCode())) {
            return false; // Asset code must be unique
        }
//        if (assetMapper.checkAssetSnExists(assetDTO.getSn())) {
//            return false; // Asset sn must be unique
//        }

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
        if (!existingAsset.getCode().equals(assetDTO.getCode()) &&
                assetMapper.checkAssetCodeExists(assetDTO.getCode())) {
            return false;
        }

        // Check for duplicate sn, excluding the current asset
//        if (!existingAsset.getSn().equals(assetDTO.getSn()) &&
//                assetMapper.checkAssetSnExists(assetDTO.getSn())) {
//            return false;
//        }

        Asset asset = mapToAsset(assetDTO);
        if (asset == null) {
            return false;
        }

        return assetMapper.updateById(asset) > 0;
    }

    private Asset mapToAsset(AssetDTO assetDTO) {
        Long assetTypeId = assetTypeMapper.getIdByName(assetDTO.getType());
        if (assetTypeId == null) {
            return null;
        }
        Long departmentId = departmentMapper.getDepartmentIdByName(assetDTO.getDepartmentName());
        if (departmentId == null) {
            return null;
        }

        Asset asset = new Asset();
        if (assetDTO.getId() != null && !assetDTO.getId().isEmpty()) {
            asset.setId(Long.valueOf(assetDTO.getId()));
        }
        asset.setName(assetDTO.getName());
        asset.setCode(assetDTO.getCode());
        asset.setType(assetTypeId);
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
    @Override
    public Workbook buildExportWorkbook(Map<String, Object> params) {
        // 1. 根据 params 调用 Mapper 拿到数据
        String name = (String) params.get("name");
        List<AssetExportDTO> list = assetMapper.getAssetExportList(0, Integer.MAX_VALUE, name);
        System.out.println("导出资产数量: " + list.size());
        for (AssetExportDTO asset : list) {
            System.out.println("资产名称: " + asset.getName() + ", 编号: " + asset.getCode());
        }
        // 检查数据是否为空
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("没有可导出的资产数据");
        }
        // 2. 转 AssetExportDTO... 或 直接用 AssetDTO 填表
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("资产列表");
        // 写表头
        Row head = sheet.createRow(0);
        String[] headers = { "ID","名称","编号","序列号","类型","型号","配置","IP","描述","供应商",
                "部门","负责人","使用人","存放地点","状态","使用状态","购入日期","购买价格","数量" };
        for (int i = 0; i < headers.length; i++) {
            head.createCell(i).setCellValue(headers[i]);
        }
        System.out.println("bug调试369");
        // 写数据行
        for (int i = 0; i < list.size(); i++) {
            AssetExportDTO dto = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(dto.getId());
            row.createCell(1).setCellValue(dto.getName());
            row.createCell(2).setCellValue(dto.getCode());
            row.createCell(3).setCellValue(dto.getSn());
            row.createCell(4).setCellValue(dto.getType());
            row.createCell(5).setCellValue(dto.getModel());
            row.createCell(6).setCellValue(dto.getConfig());
            row.createCell(7).setCellValue(dto.getIp());
            row.createCell(8).setCellValue(dto.getDescription());
            row.createCell(9).setCellValue(dto.getProvider());
            row.createCell(10).setCellValue(dto.getDepartmentName());
            row.createCell(11).setCellValue(dto.getOwnerName());
            row.createCell(12).setCellValue(dto.getUserName());
            row.createCell(13).setCellValue(dto.getLocation());
            row.createCell(14).setCellValue(dto.getStatus());
            row.createCell(15).setCellValue(dto.getUseStatus());
            row.createCell(16).setCellValue(dto.getPurchaseDate());
            row.createCell(17).setCellValue(dto.getPurchasePrice() != null ? dto.getPurchasePrice().doubleValue() : 0.0);
            row.createCell(18).setCellValue(dto.getCount());
        }
        return wb;
    }
}
