package kr.ac.jbnu.se.tetris.shapetypes;

import java.awt.Color;
import javax.swing.ImageIcon;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

public class ShapeSquare extends Shape {
    public ShapeSquare() {
        super();
        coords = new int[][]{ { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
        pieceShape = Tetrominoes.SQUARE_SHAPE;
        color = new Color(204, 102, 204);
        image = new ImageIcon("src\\main\\resources\\SquareShape.png");
    }
}