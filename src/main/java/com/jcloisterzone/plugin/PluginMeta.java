package com.jcloisterzone.plugin;

public class PluginMeta {

    private String version;
    private String title;
    private String description;
    private String author;
    private ExpansionMeta[] expansions;

    private TileImagesMeta tile_images;

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public TileImagesMeta getTile_images() {
        return tile_images;
    }
    public void setTile_images(TileImagesMeta tile_images) {
        this.tile_images = tile_images;
    }
    public ExpansionMeta[] getExpansions() {
        return expansions;
    }
    public void setExpansions(ExpansionMeta[] expansions) {
        this.expansions = expansions;
    }

    public static class TileImagesMeta {
        private String offset;
        private Integer ratio_x;
        private Integer ratio_y;

        public String getOffset() {
            return offset;
        }
        public void setOffset(String offset) {
            this.offset = offset;
        }
        public Integer getRatio_x() {
            return ratio_x;
        }
        public void setRatio_x(Integer ratio_x) {
            this.ratio_x = ratio_x;
        }
        public Integer getRatio_y() {
            return ratio_y;
        }
        public void setRatio_y(Integer ratio_y) {
            this.ratio_y = ratio_y;
        }
    }

    public static class ExpansionMeta {
        private String code;
        private String name;
        private String label;
        private String[] capabilities;
        private String type;

        public String getCode() {
            return code;
        }
        public void setCode(String code) {
            this.code = code;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public String[] getCapabilities() {
            return capabilities;
        }
        public void setCapabilities(String[] capabilities) {
            this.capabilities = capabilities;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
    }

}
