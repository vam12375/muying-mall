package com.muyingmall.dto;

import com.muyingmall.entity.Brand;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 品牌数据传输对象
 */
@Data
public class BrandDTO {

    /**
     * 品牌ID
     */
    private Integer id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌logo
     */
    private String logo;

    /**
     * 品牌描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 首字母（用于快速检索）
     */
    private String firstLetter;

    /**
     * 状态：0-禁用，1-正常
     */
    private Integer showStatus;

    /**
     * 关联商品数量
     */
    private Integer productCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 将Brand实体转换为BrandDTO
     *
     * @param brand Brand实体
     * @return BrandDTO
     */
    public static BrandDTO fromEntity(Brand brand) {
        if (brand == null) {
            return null;
        }

        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getBrandId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        dto.setDescription(brand.getDescription());
        dto.setSort(brand.getSortOrder());
        // 从名称中提取首字母，实际项目中可能需要更复杂的逻辑
        dto.setFirstLetter(brand.getName() != null && !brand.getName().isEmpty()
                ? brand.getName().substring(0, 1).toUpperCase()
                : "");
        dto.setShowStatus(brand.getStatus());
        // 商品数量需要单独查询，这里暂时设为0
        dto.setProductCount(0);
        dto.setCreateTime(brand.getCreateTime());
        dto.setUpdateTime(brand.getUpdateTime());

        return dto;
    }

    /**
     * 将Brand实体转换为BrandDTO，并设置商品数量
     *
     * @param brand        Brand实体
     * @param productCount 商品数量
     * @return BrandDTO
     */
    public static BrandDTO fromEntity(Brand brand, Integer productCount) {
        BrandDTO dto = fromEntity(brand);
        if (dto != null && productCount != null) {
            dto.setProductCount(productCount);
        }
        return dto;
    }
}