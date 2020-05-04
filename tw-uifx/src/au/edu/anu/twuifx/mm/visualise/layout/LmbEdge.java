package au.edu.anu.twuifx.mm.visualise.layout;

/**
 * See if we need an adjacency matrix 'adjMat' since every node has a neighbour
 * list.
 */
public class LmbEdge {
	// private double dist;// set on init - ideal spring length = k
	// private double factor;// set on init
	private double cx;// ctrl pt
	private double cy;// ctrl pt
	private double pcx;// ctrl pt
	private double pcy;// ctrl pt
	private double pa;// p angle, set on init
	private double qa;// q angle, set on init
	private double pf;// p rot force
	private double qf;// q rot force
	private LmbVertex p;
	private LmbVertex q;

	// if p not in nodeDict: Node(p)
//	if q not in nodeDict: Node(q)
//	if p not in adjMat: adjMat[p] = {}
//	if q not in adjMat: adjMat[q] = {}
//	adjMat[p][q] = self
//	adjMat[q][p] = self
//	self.p = nodeDict[p]
//	self.q = nodeDict[q]

	public LmbEdge(LmbVertex p, LmbVertex q) {
		this.p = p;
		this.q = q;
	}

	public void init() {
		// dist = K??
		pa = p.nextTanAngle();
		qa = q.nextTanAngle();
	}

	public LmbVertex getP() {
		return p;
	}

	public LmbVertex getQ() {
		return q;
	}

	public double setAttractionDisplacement(double k) {
		return FRVertex.attrApply(p, q, k);
	}

	// n is from
	public double tanAngle(LmbVertex n) {
		return pie2(n.getAngle() + tanAngleRel(n));
	}

	// n is from
	public double tanAngleRel(LmbVertex n) {
		if (n == p)
			return pa;
		else
			return qa;
	}

	public double edgeAngle(LmbVertex n) {
		LmbVertex n1;
		LmbVertex n2;
		if (p == n) {
			n1 = p;
			n2 = q;
		} else {
			n1 = q;
			n2 = p;
		}
		double y = -(n2.getY() - n1.getY());
		double x = (n2.getX() - n1.getX());
		return pie2(Math.atan2(y, x));
	}

	public double diffAngle(LmbVertex n) {
		return pie(pie(tanAngle(n)) - pie(edgeAngle(n)));
	}

	public void setAngle(LmbVertex n, double angle) {
		if (n == p) {
			pa = angle;
		} else {
			qa = angle;
		}
	}

	public void addForce(LmbVertex n, double f) {
		if (n == p)
			pf += f;
		else
			qf += f;
	}

	public Vector ctrlPt() {
		return new Vector(cx, cy);
	}
	// ------------------------------------

	public static double pie(double rad) {
		rad = pie2(rad);
		if (rad <= Math.PI)
			return rad;
		else
			return rad - 2 * Math.PI;
	}

	public static double pie2(double rad) {
		return rad % (2 * Math.PI);
	}

	public static double mag(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	public boolean finalStep() {
		boolean result = true;
		double pd = diffAngle(p);
		double qd = diffAngle(q);
		double optidiff = (pd - qd) / 2.0;
		double adjust = Math.abs(pd + qd) / 2.0;
		if (adjust > 0.01)
			result = false;
		if (optidiff > pd) {// increase
			if (p.degree() == 1)
				pa = LmbEdge.pie2(pa + adjust * 2.0);
			else if (q.degree() == 1)
				qa = LmbEdge.pie2(qa + adjust * 2.0);
			else {
				pa = LmbEdge.pie2(pa + adjust);
				qa = LmbEdge.pie2(qa + adjust);
			}
		} else {// decrease
			if (p.degree() == 1)
				pa = LmbEdge.pie2(pa - adjust * 2.0);
			else if (q.degree() == 1)
				qa = LmbEdge.pie2(qa - adjust * 2.0);
			else {
				pa = LmbEdge.pie2(pa - adjust * 2.0);
				qa = LmbEdge.pie2(qa = adjust * 2.0);
			}
		}
		return result;
	}

}
