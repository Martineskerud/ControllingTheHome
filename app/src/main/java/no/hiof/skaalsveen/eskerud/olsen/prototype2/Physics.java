package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import java.util.ArrayList;

import android.graphics.Canvas;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.Controller;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.PhysicalObject;

public class Physics implements Controller {

	private ArrayList<PhysicalObject> objects;

	public Physics() {
		objects = new ArrayList<PhysicalObject>();
	}

	public void add(PhysicalObject obj) {
		objects.add(obj);
	}

	@Override
	public void update(float tpf) {
		
	}

	@Override
	public void draw(Canvas canvas) {
		
	}
}
