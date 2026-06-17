package seal.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import seal.backend.entities.Season;
import seal.openapi.model.CreateSeasonRequestDto;
import seal.openapi.model.SeasonDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SeasonMapper {
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", expression = "java(seal.backend.enums.SeasonStatus.DRAFT)")
  Season toEntity(CreateSeasonRequestDto dto);

  SeasonDto toDto(Season entity);
}
