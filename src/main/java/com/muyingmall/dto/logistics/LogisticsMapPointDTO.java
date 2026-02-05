package com.muyingmall.dto.logistics;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流地图大屏点位DTO
 */
@Data
public class LogisticsMapPointDTO {

    private Long id;
    private String trackingNo;
    private String status;
    private String companyName;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private LocalDateTime lastUpdate;
    private Double senderLongitude;
    private Double senderLatitude;
    private Double receiverLongitude;
    private Double receiverLatitude;
    private Position position;

    @Data
    public static class Position {
        private Double lng;
        private Double lat;
    }
}
