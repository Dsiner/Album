package com.d.lib.album.widget.draw;

import android.graphics.Path;

import com.d.lib.album.widget.draw.action.Action;
import com.d.lib.album.widget.draw.action.Line;
import com.d.lib.album.widget.draw.action.Move;
import com.d.lib.album.widget.draw.action.Quad;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

class DrawPath extends Path implements Serializable {
    private final List<Action> mActions = new LinkedList<>();

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        for (Action action : mActions) {
            action.perform(this);
        }
    }

    public void readObject(String pathData) throws InvalidParameterException {
        String[] tokens = pathData.split("\\s+");
        for (int i = 0; i < tokens.length; ++i) {
            switch (tokens[i].charAt(0)) {
                case 'M':
                    addAction(new Move(tokens[i]));
                    break;

                case 'L':
                    addAction(new Line(tokens[i]));
                    break;

                case 'Q':
                    // Quad actions are of the following form:
                    // "Qx1,y1 x2,y2"
                    // Since we split the tokens by whitespace, we need to join them again
                    if (i + 1 >= tokens.length) {
                        throw new InvalidParameterException("Error parsing the data for a Quad.");
                    }
                    addAction(new Quad(tokens[i] + " " + tokens[i + 1]));
                    ++i;
                    break;
            }
        }
    }

    private void addAction(Action action) {
        if (action instanceof Move) {
            moveTo(((Move) action).x, ((Move) action).y);
        } else if (action instanceof Line) {
            lineTo(((Line) action).x, ((Line) action).y);
        } else if (action instanceof Quad) {
            final Quad q = (Quad) action;
            quadTo(q.x1, q.y1, q.x2, q.y2);
        }
    }

    public List<Action> getActions() {
        return mActions;
    }

    @Override
    public void moveTo(float x, float y) {
        mActions.add(new Move(x, y));
        super.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        mActions.add(new Line(x, y));
        super.lineTo(x, y);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        mActions.add(new Quad(x1, y1, x2, y2));
        super.quadTo(x1, y1, x2, y2);
    }

    @Override
    public void reset() {
        mActions.clear();
        super.reset();
    }
}
