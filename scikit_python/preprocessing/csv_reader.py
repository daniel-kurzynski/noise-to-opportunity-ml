"""
Reading CSV files in our fashion.
"""
class CSVReader():
	def read(self, filename, line_extractor):
		target_mapping = {}
		output = {
		"data": [],
		"target": [],
		"target_names": [],
		}
		with open(join(dirname(__file__), filename)) as f:
			for line in f:
				data, category = line_extractor(line)
				if category not in target_mapping:
					target_mapping[category] = len(target_mapping)
				output["data"].append(data)
				output["target"].append(target_mapping[category])

		return output


	def brochure_exractor(self, line):
		_, data, category, _ = line.replace("\\,", "<komma>").split(",")
		return data.replace("<komma>", ",")[1:-1], category


	def linked_in_extractor(self, line):
		_, data1, data2, _, _, _, _, _, category, _, _ = line.replace("\\,", "<komma>").split(",")
		data = data1[1:-1] + " " + data2[1:-1]
		return data.replace("<komma>", ","), category

