package com.pfizer.equip.searchservice.dto;

import java.time.Instant;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores search result data for file data
 * 
 * @author HeinemanWP
 *
 */
public class FileDataSearchResult {
	@Expose
	@SerializedName("jcr:path")
	private String jcrPath;
	@Expose
	@SerializedName("jcr:uuid")
	private String jcrUuid;
	@Expose
	@SerializedName("jcr:primaryType")
	private String jcrPrimaryType;
	@Expose
	@SerializedName("equip:name")
	private String equipName;
	@Expose
	@SerializedName("equip:description")
	private String equipDescription;
	@Expose
	@SerializedName("equip:itemType")
	private String equipItemType;
	@Expose
	@SerializedName("equip:columnNames")
	private String[] equipColumnNames;
	@Expose
	@SerializedName("equip:published")
	private Boolean equipPublished;
	@Expose
	@SerializedName("equip:released")
	private Boolean equipReleased;
	@Expose
	@SerializedName("equip:equipId")
	private String equipEquipId;
	@Expose
	@SerializedName("equip:qcStatus")
	private String equipQcStatus;
	@Expose
	@SerializedName("equip:dataStatus")
	private String equipDataStatus;
	@Expose
	@SerializedName("equip:promotionStatus")
	private String equipPromotionStatus;
	@Expose
	@SerializedName("equip:studyId")
	private String[] equipStudyId;
	@Expose
	@SerializedName("equip:assemblyType")
	private String equipAssemblyType;
	@Expose
	@SerializedName("equip:dataframeType")
	private String equipDataframeType;
	@Expose
	@SerializedName("equip:subType")
	private String equipSubType;
	@Expose
	@SerializedName("equip:restrictionStatus")
	private String equipRestrictionStatus;
	@Expose
	@SerializedName("equip:dataBlindingStatus")
	private String equipDataBlindingStatus;
	@Expose
	@SerializedName("equip:createdBy")
	private String equipCreatedBy;
	@Expose
	@SerializedName("equip:created")
	private Instant equipCreated;
	@Expose
	@SerializedName("equip:modifiedBy")
	private String equipModifiedBy;
	@Expose
	@SerializedName("equip:modified")
	private Instant equipModified;
	@Expose
	@SerializedName("equip:deleteFlag")
	private Boolean equipDeleteFlag;
	@Expose
	@SerializedName("equip:comment")
	private String equipComment;
	private String fileId;
	
	public String getJcrPath() {
		return jcrPath;
	}
	public void setJcrPath(String jcrPath) {
		this.jcrPath = jcrPath;
	}
	public String getJcrUuid() {
		return jcrUuid;
	}
	public void setJcrUuid(String jcrUuid) {
		this.jcrUuid = jcrUuid;
	}
	public String getJcrPrimaryType() {
		return jcrPrimaryType;
	}
	public void setJcrPrimaryType(String jcrPrimaryType) {
		this.jcrPrimaryType = jcrPrimaryType;
	}
	public String getEquipName() {
		return equipName;
	}
	public void setEquipName(String equipName) {
		this.equipName = equipName;
	}
	public String getEquipDescription() {
		return equipDescription;
	}
	public void setEquipDescription(String equipDescription) {
		this.equipDescription = equipDescription;
	}
	public String getEquipItemType() {
		return equipItemType;
	}
	public void setEquipItemType(String equipItemType) {
		this.equipItemType = equipItemType;
	}
	public String[] getEquipColumnNames() {
		return equipColumnNames;
	}
	public void setEquipColumnNames(String[] equipColumnNames) {
		this.equipColumnNames = equipColumnNames;
	}
	public Boolean getEquipPublished() {
		return equipPublished;
	}
	public void setEquipPublished(Boolean equipPublished) {
		this.equipPublished = equipPublished;
	}
	public Boolean getEquipReleased() {
		return equipReleased;
	}
	public void setEquipReleased(Boolean equipReleased) {
		this.equipReleased = equipReleased;
	}
	public String getEquipEquipId() {
		return equipEquipId;
	}
	public void setEquipEquipId(String equipEquipId) {
		this.equipEquipId = equipEquipId;
	}
	public String getEquipQcStatus() {
		return equipQcStatus;
	}
	public void setEquipQcStatus(String equipQcStatus) {
		this.equipQcStatus = equipQcStatus;
	}
	public String getEquipDataStatus() {
		return equipDataStatus;
	}
	public void setEquipDataStatus(String equipDataStatus) {
		this.equipDataStatus = equipDataStatus;
	}
	public String getEquipPromotionStatus() {
		return equipPromotionStatus;
	}
	public void setEquipPromotionStatus(String equipPromotionStatus) {
		this.equipPromotionStatus = equipPromotionStatus;
	}
	public String[] getEquipStudyId() {
		return equipStudyId;
	}
	public void setEquipStudyId(String[] equipStudyId) {
		this.equipStudyId = equipStudyId;
	}
	public String getEquipAssemblyType() {
		return equipAssemblyType;
	}
	public void setEquipAssemblyType(String equipAssemblyType) {
		this.equipAssemblyType = equipAssemblyType;
	}
	public String getEquipDataframeType() {
		return equipDataframeType;
	}
	public void setEquipDataframeType(String equipDataframeType) {
		this.equipDataframeType = equipDataframeType;
	}
	public String getEquipSubType() {
		return equipSubType;
	}
	public void setEquipSubType(String equipSubType) {
		this.equipSubType = equipSubType;
	}
	public String getEquipRestrictionStatus() {
		return equipRestrictionStatus;
	}
	public void setEquipRestrictionStatus(String equipRestrictionStatus) {
		this.equipRestrictionStatus = equipRestrictionStatus;
	}
	public String getEquipDataBlindingStatus() {
		return equipDataBlindingStatus;
	}
	public void setEquipDataBlindingStatus(String equipDataBlindingStatus) {
		this.equipDataBlindingStatus = equipDataBlindingStatus;
	}
	public String getEquipCreatedBy() {
		return equipCreatedBy;
	}
	public void setEquipCreatedBy(String equipCreatedBy) {
		this.equipCreatedBy = equipCreatedBy;
	}
	public Instant getEquipCreated() {
		return equipCreated;
	}
	public void setEquipCreated(Instant equipCreated) {
		this.equipCreated = equipCreated;
	}
	public String getEquipModifiedBy() {
		return equipModifiedBy;
	}
	public void setEquipModifiedBy(String equipModifiedBy) {
		this.equipModifiedBy = equipModifiedBy;
	}
	public Instant getEquipModified() {
		return equipModified;
	}
	public void setEquipModified(Instant equipModified) {
		this.equipModified = equipModified;
	}
	public Boolean getEquipDeleteFlag() {
		return equipDeleteFlag;
	}
	public void setEquipDeleteFlag(Boolean equipDeleteFlag) {
		this.equipDeleteFlag = equipDeleteFlag;
	}
	public String getEquipComment() {
		return equipComment;
	}
	public void setEquipComment(String equipComment) {
		this.equipComment = equipComment;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	
}
