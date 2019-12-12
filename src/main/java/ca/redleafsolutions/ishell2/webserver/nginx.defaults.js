{
	path: "nginx",
	host: "127.0.0.1",
	port: 80,
	root: "",
	docroot: "htdoc",
	routing: [
		{
			location: "/",
			root: "htdoc"
		}
	],
	ishellhttp: {
		host: "127.0.0.1",
		port: 81
	},
}