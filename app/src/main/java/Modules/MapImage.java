package Modules;

/**
 * Created by CianFDoherty on 29-Mar-18.
 */

public class MapImage {

    public String imageName;

    public String imageURL;

    public MapImage() {

    }

    public MapImage(String name, String url) {

        this.imageName = name;
        this.imageURL= url;
    }

    public String getMapName() {
        return imageName;
    }

    public String getMapURL() {
        return imageURL;
    }
}
