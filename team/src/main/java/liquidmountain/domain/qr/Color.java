package liquidmountain.domain.qr;

/**
 * This class represents a color
 */
public class Color {
    private String color;

    public Color(String hexColor) {
        this.color = hexColor;
    }

    public String getHexColor() {
        return color;
    }

}
