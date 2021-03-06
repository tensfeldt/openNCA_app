package com.pfizer.pgrd.equip.modeshape.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.pfizer.pgrd.equip.dataframe.dto.Assembly;
import com.pfizer.pgrd.equip.dataframe.dto.Comment;
import com.pfizer.pgrd.equip.dataframe.dto.EquipObject;
import com.pfizer.pgrd.equip.dataframe.dto.Metadatum;
import com.pfizer.pgrd.equip.dataframe.dto.ReportingEventStatusChangeWorkflow;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipCreatedMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipDeleteMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipIdMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipLockMixin;
import com.pfizer.pgrd.equip.modeshape.node.mixin.EquipVersionMixin;

public class AssemblyNode extends ModeShapeNode implements EquipCreatedMixin, EquipDeleteMixin, EquipIdMixin, EquipVersionMixin, EquipLockMixin {
	public static final String PRIMARY_TYPE = "equip:assembly";
	
	@Expose
	@SerializedName("equip:assemblyType")
	private String assemblyType;
	
	@Expose
	@SerializedName("equip:created")
	private Date created;
	
	@Expose
	@SerializedName("equip:createdBy")
	private String createdBy;
	
	@Expose
	@SerializedName("equip:modified")
	private Date modified;
	
	@Expose
	@SerializedName("equip:modifiedBy")
	private String modifiedBy;
	
	@Expose
	@SerializedName("equip:equipId")
	private String equipId;
	
	@Expose
	@SerializedName("equip:deleteFlag")
	private boolean isDeleted;
	
	@Expose
	@SerializedName("equip:obsoleteFlag")
	private boolean isObsolete;
	
	@Expose
	@SerializedName("equip:lockFlag")
	private boolean isLocked;
	
	@Expose
	@SerializedName("equip:lockedByUser")
	private String lockedByUser;
	
	@Expose
	@SerializedName("equip:protocolIds")
	private List<String> protocolIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:projectIds")
	private List<String> projectIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:programIds")
	private List<String> programIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:assemblyIds")
	private List<String> assemblyIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:dataframeIds")
	private List<String> dataframeIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:parentIds")
	private List<String> parentIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:loadStatus")
	private String loadStatus;
	
	@Expose
	@SerializedName("equip:qcStatus")
	private String qcStatus;
	
	@Expose
	@SerializedName("equip:reportingEventItemIds")
	private List<String> reportingEventItemIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:publishedItemIds")
	private List<String> publishedItemIds = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:versionNumber")
	private long versionNumber;
	
	@Expose
	@SerializedName("equip:versionSuperSeded")
	private boolean isSuperseded;
	
	@Expose
	@SerializedName("equip:versionCommitted")
	private boolean isCommitted;
	
	@Expose
	@SerializedName("equip:studyId")
	private List<String> studyId = new ArrayList<>();
	
	@Expose
	@SerializedName("equip:name")
	private String name;

	@Expose
	@SerializedName("equip:itemType")
	private String itemType;
	
	@Expose
	@SerializedName("equip:description")
	private String description;
	
	@Expose
	@SerializedName("equip:published")
	private boolean isPublished;
	
	@Expose
	@SerializedName("equip:released")
	private boolean isReleased;
	

	public AssemblyNode() {
		this(null);
	}
	
	public AssemblyNode(Assembly assembly) {
		super();
		this.setPrimaryType(AssemblyNode.PRIMARY_TYPE);
		this.setNodeName(this.getPrimaryType());
		
		this.populate(assembly);
	}
	
	public static List<Assembly> toAssembly(List<AssemblyNode> assemblies) {
		List<Assembly> list = new ArrayList<>();
		if(assemblies != null) {
			for(AssemblyNode dto : assemblies) {
				Assembly a = dto.toAssembly();
				list.add(a);
			}
		}
		
		return list;
	}
	
	@Override
	public EquipObject toEquipObject() {
		Assembly assembly = new Assembly();
		this.populateAssembly(assembly);
		
		return assembly;
	}
	
	protected void populateAssembly(Assembly assembly) {
		if(assembly != null) {
			assembly.setAssemblyIds(this.getAssemblyIds());
			assembly.setAssemblyType(this.getAssemblyType());
			assembly.setCreated(this.getCreated());
			assembly.setCreatedBy(this.getCreatedBy());
			assembly.setDataframeIds(this.getDataframeIds());
			assembly.setDeleteFlag(this.isDeleted());
			assembly.setDescription(this.getDescription());
			assembly.setLocked(this.isLocked());
			assembly.setLockedByUser(this.getLockedByUser());
			assembly.setEquipId(this.getEquipId());
			assembly.setId(this.getJcrId());
			assembly.setLibraryReferences(this.getPublishedItemIds());
			assembly.setLoadStatus(this.getLoadStatus());
			assembly.setModifiedDate(this.getModified());
			assembly.setModifiedBy(this.getModifiedBy());
			assembly.setObsoleteFlag(this.isObsolete());
			assembly.setProgramIds(this.getProgramIds());
			assembly.setProjectIds(this.getProjectIds());
			assembly.setProtocolIds(this.getProtocolIds());
			assembly.setPublished(this.isPublished());
			assembly.setQcStatus(this.getQcStatus());
			assembly.setReleased(this.isReleased());
			assembly.setReportingItemIds(this.getReportingEventItemIds());
			assembly.setVersionSuperSeded(this.isSuperseded());
			assembly.setVersionNumber(this.getVersionNumber());
			assembly.setCommitted(this.isCommitted());
			assembly.setParentIds(this.getParentIds());
			assembly.setStudyIds(this.getStudyId());
			assembly.setName(this.getName());
			assembly.setItemType(this.getItemType());
			
			List<Comment> comments = CommentNode.toComment(this.getComments());
			List<Metadatum> metadata = MetadatumNode.toMetadatum(this.getMetadata());
			List<ReportingEventStatusChangeWorkflow> scws = ReportingEventStatusChangeNode.toReportingEventStatusChangeWorkflow(this.getReportingEventStatusChangeWorkflow());
			assembly.setComments(comments);
			assembly.setMetadata(metadata);
			assembly.setReportingEventStatusChangeWorkflows( scws );
		}
	}
	
	private List<ReportingEventStatusChangeNode> getReportingEventStatusChangeWorkflow() {
		return this.getChildren(ReportingEventStatusChangeNode.class);
	}

	public Assembly toAssembly() {
		return (Assembly) this.toEquipObject();
	}
	
	public static List<AssemblyNode> fromAssembly(List<Assembly> assemblies) {
		List<AssemblyNode> list = new ArrayList<>();
		if(assemblies != null) {
			for(Assembly a : assemblies) {
				AssemblyNode dto = new AssemblyNode(a);
				list.add(dto);
			}
		}
		
		return list;
	}
	
	public void populate(Assembly assembly) {
		if(assembly != null) {
			this.setAssemblyIds(assembly.getAssemblyIds());
			this.setAssemblyType(assembly.getAssemblyType());
			this.setCreated(assembly.getCreated());
			this.setCreatedBy(assembly.getCreatedBy());
			this.setDataframeIds(assembly.getDataframeIds());
			this.setDeleted(assembly.isDeleteFlag());
			this.setDescription(assembly.getDescription());
			this.setLocked(assembly.isLocked());
			this.setLockedByUser(assembly.getLockedByUser());
			this.setEquipId(assembly.getEquipId());
			this.setLoadStatus(assembly.getLoadStatus());
			this.setModified(assembly.getModifiedDate());
			this.setModifiedBy(assembly.getModifiedBy());
			this.setObsolete(assembly.isObsoleteFlag());
			this.setProgramIds(assembly.getProgramIds());
			this.setProjectIds(assembly.getProjectIds());
			this.setProtocolIds(assembly.getProtocolIds());
			this.setPublishedItemIds(assembly.getPublishItemIds());
			this.setPublished(assembly.isPublished());
			this.setQcStatus(assembly.getQcStatus());
			this.setReleased(assembly.isReleased());
			this.setReportingEventItemIds(assembly.getReportingItemIds());
			this.setVersionNumber(assembly.getVersionNumber());
			this.setSuperseded(assembly.getVersionSuperSeded());
			this.setCommitted(assembly.isCommitted());
			this.setParentIds(assembly.getParentIds());
			this.setStudyId(assembly.getStudyIds());
			this.setName(assembly.getName());
			this.setItemType(assembly.getItemType());
			
			List<CommentNode> comments = CommentNode.fromComment(assembly.getComments());
			List<MetadatumNode> metadata = MetadatumNode.fromMetadatum(assembly.getMetadata());
			List<ReportingEventStatusChangeNode> statusChangeWorkflows = ReportingEventStatusChangeNode.fromReportingEventStatusChangeWorkflow(assembly.getReportingEventStatusChangeWorkflows());
			this.setComments(comments);
			this.setMetadata(metadata);
			this.setReportingEventStatusChangeWorkflow(statusChangeWorkflows);
		}
	}
	
	@Override
	public String generateNodeName() {
		return "AS-" + new Date().getTime();
	}
	
	public List<MetadatumNode> getMetadata() {
		return this.getChildren(MetadatumNode.class);
	}
	
	public void setReportingEventStatusChangeWorkflow(List<ReportingEventStatusChangeNode> scws){
		this.replaceChildren("equip:reportingEventStatusChangeWorkflow", scws);
	}
	
	public void setMetadata(List<MetadatumNode> metadata) {
		this.replaceChildren("equip:metadatum", metadata);
	}
	
	public List<CommentNode> getComments() {
		return this.getChildren(CommentNode.class);
	}
	
	public void setComments(List<CommentNode> comments) {
		this.replaceChildren(CommentNode.class, comments);
	}
	
	public List<ScriptNode> getScripts() {
		return this.getChildren(ScriptNode.class);
	}
	
	public void setScripts(List<ScriptNode> scripts) {
		this.replaceChildren(ScriptNode.class, scripts);
	}

	public String getAssemblyType() {
		return assemblyType;
	}

	public void setAssemblyType(String assemblyType) {
		this.assemblyType = assemblyType;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getEquipId() {
		return equipId;
	}

	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean isObsolete() {
		return isObsolete;
	}

	public void setObsolete(boolean isObsolete) {
		this.isObsolete = isObsolete;
	}

	public List<String> getProtocolIds() {
		return protocolIds;
	}

	public void setProtocolIds(List<String> protocolIds) {
		this.protocolIds = protocolIds;
	}

	public List<String> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<String> projectIds) {
		this.projectIds = projectIds;
	}

	public List<String> getProgramIds() {
		return programIds;
	}

	public void setProgramIds(List<String> programIds) {
		this.programIds = programIds;
	}

	public List<String> getAssemblyIds() {
		return assemblyIds;
	}

	public void setAssemblyIds(List<String> assemblyIds) {
		this.assemblyIds = assemblyIds;
	}

	public List<String> getDataframeIds() {
		return dataframeIds;
	}

	public void setDataframeIds(List<String> dataframeIds) {
		this.dataframeIds = dataframeIds;
	}

	public List<String> getParentIds() {
		return parentIds;
	}

	public void setParentIds(List<String> parentIds) {
		this.parentIds = parentIds;
	}

	public String getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}

	public String getQcStatus() {
		return qcStatus;
	}

	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}

	public List<String> getReportingEventItemIds() {
		return reportingEventItemIds;
	}
	
	public void setReportingEventItemIds(List<String> reportingEventItemIds) {
		this.reportingEventItemIds = reportingEventItemIds;
	}

	public List<String> getPublishedItemIds() {
		return publishedItemIds;
	}

	public void setPublishedItemIds(List<String> publishedItemIds) {
		this.publishedItemIds = publishedItemIds;
	}

	public long getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(long versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public boolean isSuperseded() {
		return isSuperseded;
	}

	public void setSuperseded(boolean isSuperseded) {
		this.isSuperseded = isSuperseded;
	}

	public boolean isCommitted() {
		return isCommitted;
	}

	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}

	public List<String> getStudyId() {
		return studyId;
	}

	public void setStudyId(List<String> studyId) {
		this.studyId = studyId;
	}
	
	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	
	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	public String getLockedByUser() {
		return lockedByUser;
	}

	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isPublished() {
		return isPublished;
	}

	public void setPublished(boolean published) {
		this.isPublished = published;
	}
	
	public boolean isReleased() {
		return isReleased;
	}

	public void setReleased(boolean released) {
		this.isReleased = released;
	}
}
