package cin.ufpe.br.model;



import java.awt.image.BufferedImage;
import java.io.Serializable;

public class PropriedadesFace2 implements Serializable{
	private static final long serialVersionUID = 8407202047971608648L;

	private int x;
	private int y;
	private int width;
	private int height;
	private int faces;
	private byte[] imagemFinal;
	private BufferedImage imageCortada;

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public int getFaces() {
        return faces;
    }
    public void setFaces(int x) {
        this.faces = x;
    }
    public byte[] getImagemFinal() {
        return imagemFinal;
    }
    public void setImagemFinal(byte[] y) {
        this.imagemFinal = y;
    }
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public BufferedImage getImageCortada() {
		return imageCortada;
	}
	public void setImageCortada(BufferedImage imageCortada) {
		this.imageCortada = imageCortada;
	}
}
