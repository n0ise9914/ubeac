package io.ubeac.app.features.home;

public class DrawerItem {
    private String name;
    private int icon;
    private int count;
    private DrawerItemType type;

    public DrawerItem() {
    }

    public DrawerItemType getType() {
        return type;
    }

    public void setType(DrawerItemType type) {
        this.type = type;
    }

    public DrawerItem(String name, int icon, int count, DrawerItemType type) {
        this.type = type;
        this.name = name;
        this.icon = icon;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
