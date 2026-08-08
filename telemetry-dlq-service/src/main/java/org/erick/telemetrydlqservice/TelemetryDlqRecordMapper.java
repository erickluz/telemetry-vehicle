package org.erick.telemetrydlqservice;

import org.erick.telemetrydlqservice.dto.TelemetryDlqRecordDto;
import org.erick.telemetrydlqservice.model.TelemetryDlqRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TelemetryDlqRecordMapper {

    TelemetryDlqRecordMapper MAPPER = Mappers.getMapper(TelemetryDlqRecordMapper.class);

    TelemetryDlqRecordDto toDto(TelemetryDlqRecord record);

    TelemetryDlqRecord toEntity(TelemetryDlqRecordDto dto);

}
