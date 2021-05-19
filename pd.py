
# download movie postures
# Oct 15 2020
# author: kaleab
import os
from urllib.request import urlopen
import json
import sys
class scan_for_movies():
	def __init__(self):
		self.FILE_NOT_FOUND=1
		self.WRITE_TO_FILE=False
		self.file_name='movieslist.txt'
		
		self.bad_chars = [';',':','-','_','\\','/','@','#','(',')']
		self.cwd=os.getcwd()
		
	def scan(self):
		movlist=None
		try:
			movlist = os.listdir(self.cwd)
		except:
			print('unable to read frome drive, check your privelages for this dir')
			sys.exit(1)
		mov_nm_list=[]
		count=0
		for mov in movlist:
			movname=self.getmovname(mov)
			mov_nm_list.append(movname)
			count=count+1
		print(count,' items found')
		self.movies=mov_nm_list
		if self.WRITE_TO_FILE!=False:
			self.write_to_file()

	def get_list_from_file(self,filename):
		if os.path.exists(filename):
			movies=[]
			count=1
			with open(filename,'r') as f:
				line=f.readline()
				movies.append(line)
				while line != '':
					line=f.readline()
					if line != '':
						movies.append(line)
						count+=1
				return (count,movies)
		else:
			return (None,None)

	def getmovname(self,mov):
		# remove unwanted characters from the string(movie name)
		temp = mov.split('2')[0]
		temp=temp.split('1')[0]
		base=os.path.basename(temp)
		no_ext=os.path.splitext(base)[0]
		movie_name=no_ext.replace('.',' ')
		for char in self.bad_chars:
			movie_name=movie_name.replace(char,' ')
		movie_name=movie_name.strip()
		movie_name=movie_name.replace(' ','_')
		return movie_name

		# return character free movie names list, that are in the current dir
	def getmovies(self):
		return self.movies

	# write movies list to a txt file
	def write_to_file(self):
		if self.movies != None:
			with open(self.file_name,'w') as f:
				for movie in self.movies:
					f.write(movie+'\n')
			print('movies list dumped successfully.')




class movie_postures():
	import random
	def __init__(self):
		self.api="http://www.omdbapi.com/?apikey=b5797cc3&s=NAME"
		self.key='b5797cc3'
		self.arg_name='NAME'
	def get_poster_url(self,name):
		try:
			url=self.api.replace(self.arg_name,name)
			response=urlopen(url)
			data=response.read().decode('utf-8')
			response.close()
			data = json.loads(data)
			print(data)
			search=data['Search']
			total_results=data['totalResults']

			movdat=search[0]
			mov=movdat['Title']
			imgurl=movdat['Poster']
			return imgurl
		except:
			return None

	def download_image(self,mov_name,url):
		if url != 'N/A':
			retry = 0
			try:
				res=urlopen(url)
				img=res.read()
				res.close()
				file=open('%s'%mov_name,random.randint(0-99999),'wb')
				file.write(img)
				file.close()
			except ssl.SSLError:
				print("ssl error, retrying")
				retry+=1
				if retry > 3:
					return false
				self.download_image(mov_name,url)

def download(movies):
	print('starting download')
	mp=movie_postures()
	for movie in movies:
		imgurl=mp.get_poster_url(movie)
		if imgurl != None:
			print(movie)
			mp.download_image(movie,imgurl)
		else:
			print('error in method download(movies) line 116')

def main():
	option=None
	try:
		option=sys.argv[1]
	except:
		pass
	if option!=None:
		if option == '--fromfile':
			filename=None
			try:
				filename=sys.argv[2]
			except:
				print('you have to pass a file name. check help for more info')
				sys.exit(1)
			helper=scan()
			movies=helper.get_list_from_file(filename) # return value is a tuple object (count,movlist)
			if movielist[0]==None:
				print('cound not found the file: %s'%filename)
				sys.exit(1)
			print(movies[0],' : movies found')
			download(movies[1])
		if option=='-f':
			helper=scan_for_movies()
			helper.WRITE_TO_FILE=True
			helper.scan()
			movies=helper.getmovies()
			download(movies)
	else:
		print('scanning for movies in the current directory')
		helper=scan_for_movies()
		helper.scan()
		movies=helper.getmovies()
		download(movies)

print('poster downloader by kaleab')
main()
input('press any key to exit')


			