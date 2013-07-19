from core import ASUnion

class GeoEntity(ASUnion):
	def __init__(self):
		super(GeoEntity, self).__init__()
		self.structure = None
		self.unit = None
		self.material = None
